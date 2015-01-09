import processing.video.*;
import processing.serial.*;

Serial serialPort;
Capture cam;
Animation animation;
Filmstrip filmstrip;
Viewer viewer;
ScrollBar scrollBar;
Player player;
PImage camImage;
Message message;
long scrubStopTime=0;

byte[] serialBuff= new byte[3];
byte currentButton= 'n';

int maxNumFrames= 250;

Resetter resetter;

void setup() {
  size(displayWidth, displayHeight, P2D);
  
  println(Serial.list());
  //serialPort= new Serial(this, "COM7", 9600);
  serialPort= new Serial(this, Serial.list()[5], 9600);
  serialPort.bufferUntil(-128);
  serialPort.clear();
  
  cam= new Capture(this, 640, 480);
  cam.start();
    
  animation= new Animation(maxNumFrames);
  scrollBar= new ScrollBar(0,height-64,width,16);
  filmstrip= new Filmstrip();
  viewer= new Viewer();
  player= new Player(10);
  resetter= new Resetter();
  message= new Message();
  
  //animation.addLeader();
}

boolean sketchFullScreen() {
  return true;
}

void draw() {
  background(30);
  
  if (cam.available() == true){
    cam.read();
    camImage= cam.get();
  } 
  
  if(!player.playing){
    filmstrip.updateDisplay();
    viewer.update();
    scrollBar.update();
    resetter.update();
  }
  else player.update();
  
  updateWheel();
  readButtons();
  
  message.update();
  
  fill(200);
  if (!player.playing) text(animation.numFrames, 10,30);
}

void keyPressed(){
  if(key == ' ') takeFrame();
  if(key == 'w') player.playMovie();
  if(key == 'a') resetter.requestReset(); // must match keyReleased key below
  if(key == 's') kill();
  if(key == 'd') writeFile();
}

void keyReleased(){
  if(key == 'a') resetter.stopRequest();
}

void mouseMoved(){
  
}

void serialEvent(Serial p){
  p.readBytes(serialBuff);
  if (serialBuff[2] != -128) p.clear();
  else if (serialBuff[1] != 'n') if (currentButton=='n') currentButton= serialBuff[1];
  byte c= serialBuff[1];
  //if (c != 'n') println(char(c));
}

void updateWheel(){  
  float vel= float(serialBuff[0]) / 20;
  if(viewer.scrubbing){
    animation.movePlayhead(vel);
    if (abs(vel) > 0.05) viewer.lastScrubTime= millis();
    if (millis() > viewer.lastScrubTime + 4000){
      viewer.scrubbing= false;
      scrubStopTime=millis();
      animation.playhead= animation.numFrames;      
    }
  }
  
  else if(abs(vel) > 0.2){
    if(!viewer.scrubbing && animation.numFrames > 1){
      viewer.scrubbing= true;
      animation.movePlayhead(vel);
      viewer.lastScrubTime= millis();
    }
  }
}

void readButtons(){
  byte in= currentButton;
  currentButton= 'n'; 
  
  if(in == 'p') if(player.playing) player.stopPlaying(); else if (animation.numFrames > 0) player.playMovie();
  if(in == 'd') if(viewer.scrubbing){
    if (!filmstrip.frameBeingRemoved) filmstrip.removeFrame(); 
    viewer.lastScrubTime= millis();
  }
  else resetter.requestReset();
  if(in == 't') takeFrame();
  if(in == 's') writeFile();
  if(in == 'D') resetter.stopRequest();
}

void takeFrame(){
  if(animation.numFrames < maxNumFrames && !viewer.scrubbing && !filmstrip.frameBeingAdded && !player.playing && millis() > scrubStopTime+500 && millis() > filmstrip.lastFrameAddTime+500) filmstrip.addFrame(cam.get());
  
  if(viewer.scrubbing){
    viewer.scrubbing= false;
    scrubStopTime= millis();
    animation.playhead= animation.numFrames;      
  }
  
  if(player.playing) player.stopPlaying();
}

void writeFile(){
  File dir;
  dir= new File(System.getProperty("user.home")+"/Documents/stop_motion");
  //println(dir.getAbsolutePath());
  //println(dir.dlist());
  int numFiles= dir.list().length;
  
  //MovieMaker mm= new MovieMaker(this, 640, 480, dir.getAbsolutePath()+"/"+numFiles+".mov", 
  //    player.fps, MovieMaker.H263, MovieMaker.HIGH);
  
  for(int i=0; i < animation.numFrames; i++){
    //animation.animation[i].loadPixels();
    //mm.addFrame(animation.animation[i].pixels, 640,480);
    animation.animation[i].save(dir.getAbsolutePath()+"/"+numFiles+"/"+i+".jpg");
  }
  //mm.finish();
  println("movie file saved");
  
  message.setMessage("Movie saved in " + numFiles + "/");
}

void kill(){
  //serialPort.stop();
  exit();
}



