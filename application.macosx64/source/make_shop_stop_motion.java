import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.video.*; 
import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class make_shop_stop_motion extends PApplet {




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

public void setup() {
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

public boolean sketchFullScreen() {
  return true;
}

public void draw() {
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

public void keyPressed(){
  if(key == ' ') takeFrame();
  if(key == 'w') player.playMovie();
  if(key == 'a') resetter.requestReset(); // must match keyReleased key below
  if(key == 's') kill();
  if(key == 'd') writeFile();
}

public void keyReleased(){
  if(key == 'a') resetter.stopRequest();
}

public void mouseMoved(){
  
}

public void serialEvent(Serial p){
  p.readBytes(serialBuff);
  if (serialBuff[2] != -128) p.clear();
  else if (serialBuff[1] != 'n') if (currentButton=='n') currentButton= serialBuff[1];
  byte c= serialBuff[1];
  //if (c != 'n') println(char(c));
}

public void updateWheel(){  
  float vel= PApplet.parseFloat(serialBuff[0]) / 20;
  if(viewer.scrubbing){
    animation.movePlayhead(vel);
    if (abs(vel) > 0.05f) viewer.lastScrubTime= millis();
    if (millis() > viewer.lastScrubTime + 4000){
      viewer.scrubbing= false;
      scrubStopTime=millis();
      animation.playhead= animation.numFrames;      
    }
  }
  
  else if(abs(vel) > 0.2f){
    if(!viewer.scrubbing && animation.numFrames > 1){
      viewer.scrubbing= true;
      animation.movePlayhead(vel);
      viewer.lastScrubTime= millis();
    }
  }
}

public void readButtons(){
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

public void takeFrame(){
  if(animation.numFrames < maxNumFrames && !viewer.scrubbing && !filmstrip.frameBeingAdded && !player.playing && millis() > scrubStopTime+500 && millis() > filmstrip.lastFrameAddTime+500) filmstrip.addFrame(cam.get());
  
  if(viewer.scrubbing){
    viewer.scrubbing= false;
    scrubStopTime= millis();
    animation.playhead= animation.numFrames;      
  }
  
  if(player.playing) player.stopPlaying();
}

public void writeFile(){
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

public void kill(){
  //serialPort.stop();
  exit();
}



class Animation {
  private int maxLength;
  public PImage[] animation;
  public int numFrames;
  public float playhead; // pos in frames 
  long lastFrameRemoveTime=0;
  
  
  PImage frameMask;
  PImage emptyFrame;
 
  Animation(int _maxLength){
    maxLength= _maxLength;
    animation= new PImage[maxLength];
    numFrames= 0;
    playhead= 0;
    
    frameMask= loadImage("frame_mask.png");
    emptyFrame= loadImage("empty_frame.jpg");
    
    emptyFrame.mask(frameMask);
  }

  public void addFrame(PImage frame){
    if (numFrames == maxLength) return;
    animation[numFrames]= frame;
    numFrames++;
  }
  
  public void removeFrame(){
    if (millis() < lastFrameRemoveTime + 250) return;
    int whichFrame= floor(playhead)-1;
    if(numFrames>0){
      numFrames--;
      for(int i=whichFrame; i<numFrames; i++) animation[i]= animation[i+1];
    }
    lastFrameRemoveTime= millis();
  }
  
  public PImage getNextFrame(){
    PImage nextFrame= animation[floor(playhead)];
    playhead++;
    if(playhead==numFrames) playhead=0;
    return nextFrame;
  }
  
  public PImage getFrame(int index){
    if (index < 0 || index >= numFrames) return emptyFrame;
    if (animation[index] == null) return emptyFrame;
    return animation[index];
  }
  
  public PImage maskFrame(PImage frame){
    frame.mask(frameMask);
    return frame;
  }
  
  public void movePlayhead(float amt){
    playhead+= amt;
    if (playhead > numFrames) playhead= numFrames;
    if (playhead < 1) playhead= 1;
  }
  
  public void clearAnimation(){
    numFrames=0;
    playhead=0;
  }
}



// transistions . . . 

class Filmstrip{
  int frameAddDuration= 300; // time for frame adding animation, in ms
  //int scrollMaxDuration= 500; // scrubbing inactivity timeout
  
  int captureX= 0;
  int captureH= height/15;
  int captureY= scrollBar.y-captureH-10; 
  int captureW= width;
  
  int scrubX= 0;
  int scrubH= height/5;
  int scrubY= scrollBar.y-scrubH-10;
  int scrubW= width;
  
  int scrubPlayheadPos;
  private int capturePlayheadPos;
  
  int x,y,w,h;
  int numVisibleFrames;
  int frameWidth;
  int lastFrameAddTime= 0-frameAddDuration; //avoid false triggering
  boolean frameBeingAdded= false;
  int lastFrameRemoveTime=0-frameAddDuration;
  boolean frameBeingRemoved= false;
  
  int scrollTarget;
  float scrollStartPos;
  PImage newFrame;
  float adderProgress;
  
  Filmstrip(){
    x= captureX;
    y= captureY;
    w= captureW;
    h= captureH;
    
    //assuming a 4:3 aspect ratio...
    frameWidth= (h/3)*4;

    scrubPlayheadPos= w/2;
    capturePlayheadPos= w-frameWidth/2;
    
    // round up for fractional frames, add one for a panning buffer
    numVisibleFrames= ceil((PApplet.parseFloat(w)/frameWidth))+2;
  }
  
  public void updateDisplay(){
    if(viewer.scrubbing){
          x= scrubX;
          y= scrubY;
          w= scrubW;
          h= scrubH;
              frameWidth= (h/3)*4;
                  numVisibleFrames= ceil((PApplet.parseFloat(w)/frameWidth))+2;


        }
        else{
          x= captureX;
          y= captureY;
          w= captureW;
          h= captureH;
              frameWidth= (h/3)*4;
                  numVisibleFrames= ceil((PApplet.parseFloat(w)/frameWidth))+2;


        }
        
    if(frameBeingAdded){
      adderProgress= map(millis(), lastFrameAddTime, lastFrameAddTime+frameAddDuration, 0, 1);
      if (adderProgress > 1.0f) adderProgress= 1.0f;

      animation.playhead=animation.numFrames+adderProgress;
      drawStrip(w-frameWidth/2);

      // float incoming frame towards strip...
      int floaterX= PApplet.parseInt(map(adderProgress, 0, 1, viewer.x, x+w-frameWidth));
      int floaterY= PApplet.parseInt(map(adderProgress, 0, 1, viewer.y, y));
      int floaterW= PApplet.parseInt(map(adderProgress, 0, 1, viewer.w, frameWidth));
      int floaterH= PApplet.parseInt(map(adderProgress, 0, 1, viewer.h, h));
      // ...and fade it out as it approaces
      //tint(255, int(map(adderProgress, 0, 1, 255, 0)));
      image(newFrame, floaterX, floaterY, floaterW, floaterH);
      noTint();
      
      if(millis() >= lastFrameAddTime + frameAddDuration){
        frameBeingAdded= false;
        adderProgress= 0;
        animation.addFrame(newFrame);
      }
    }
    
    else if(frameBeingRemoved){
      adderProgress= map(millis(), lastFrameRemoveTime, lastFrameRemoveTime+frameAddDuration, 0, 1);
      if (adderProgress > 1.0f) adderProgress= 1.0f;
      
      drawStrip(scrubPlayheadPos);
      
      if(millis() >= lastFrameRemoveTime + frameAddDuration){
        frameBeingRemoved= false;
        adderProgress= 0;
        animation.removeFrame();
      }
    }
    
    else if(viewer.scrubbing) drawStrip(scrubPlayheadPos);
    else drawStrip(capturePlayheadPos);
  }
  
  // animation.playhead is in frames and fractions of frames, 
  // playheadPos is the center of the playhead frame in pixels
  public void drawStrip(float playheadPos){
    int offset= PApplet.parseInt((animation.playhead - floor(animation.playhead)) * frameWidth);
    
    PImage frame;
    
    // draw playhead frame...
    frame= animation.getFrame(floor(animation.playhead)-1).get();
    frame= animation.maskFrame(frame);
    if (frameBeingRemoved) tint(255, 255-255.0f*adderProgress); // dim playhead frame if removing it
    image (frame, x+playheadPos-frameWidth/2-offset, y, frameWidth, h);
    
    // ...then, draw the frames to its left...
    int numFramesOnLeft= PApplet.parseInt((playheadPos/w)*(numVisibleFrames-1));
    tint(255,127); //make all but playhead frames dimmed
    for(int i=1; i <= numFramesOnLeft; i++){
      frame= animation.getFrame(floor(animation.playhead)-i-1).get();
      frame= animation.maskFrame(frame);
      image(frame, x+playheadPos-frameWidth/2-offset-frameWidth*i, y, frameWidth, h);
    }
    // ...and the frames to its right-
    if (frameBeingRemoved){ pushMatrix(); translate(-frameWidth*adderProgress, 0); }
    for(int i=1; i <= numVisibleFrames-numFramesOnLeft-1; i++){
      frame= animation.getFrame(floor(animation.playhead)+i-1).get();
      frame= animation.maskFrame(frame);
      image(frame, x+playheadPos-frameWidth/2-offset+frameWidth*i, y, frameWidth, h);
    }
    if (frameBeingRemoved) popMatrix();
    noTint();
  }
    
  public void addFrame(PImage frame){
    newFrame= frame;
    lastFrameAddTime= millis();
    frameBeingAdded= true;
  }
  
  public void removeFrame(){
    lastFrameRemoveTime= millis();
    frameBeingRemoved= true;
  }
}
class Message{
  int startTime;
  int lastReset=0;
  boolean resetRequesting= false;
  PFont font;
  int heldSeconds;
  
  int w= 600;
  int h= 80;
  int x= width/2-w/2;
  int y= height/2-2*h/3;
  
  String msg;
  
  Message(){
    font = loadFont("Helvetica-48.vlw");
    textFont(font, 32);  
  }
  
  public void update(){
    if(millis() < startTime+5000){
      if(msg != null) drawMessage();
    }
    else msg= null;
    
    
  }
  
  public void setMessage(String _msg){
    msg= _msg;
    startTime= millis();
  }
  
  public void drawMessage(){
      fill(100);
      rect(x,y,w,h);
      fill(255);
      if (msg != null) text(msg, x+20,y+20,w-40,h-40);
  }    
}
  
class Player{
  boolean playing= false;
  int fps;
  int msPerFrame;
  int currentFrameStart; //  <- time at which a frame is displayed, wait for msPerFrame for the next one... 
  int currentFrame; // <- animation frame index counter
  
  Player(int _fps){
    fps= _fps;
    msPerFrame= 1000/fps;
  }
  
  public void playMovie(){
    playing= true;
    currentFrameStart= millis();
    currentFrame= 0;
  }
  
  public void update(){
    image(animation.animation[currentFrame], 0, 0, width, height);
    if(millis() > currentFrameStart+msPerFrame){
      currentFrame++;
      currentFrameStart=millis();
    }
    if(currentFrame >= animation.numFrames) currentFrame= 0;       
  }
  
  public void stopPlaying(){
    playing= false;
  }
}

class Resetter{
  int requestStartTime;
  int lastReset=0;
  boolean resetRequesting= false;
  PFont font;
  int heldSeconds;
  
  int w= 600;
  int h= 80;
  int x= width/2-w/2;
  int y= height/2-2*h/3;
  
  Resetter(){
    font = loadFont("Helvetica-48.vlw");
    
    textFont(font, 32);
  }
  
  public void update(){
    if(resetRequesting){
      heldSeconds= (millis()-requestStartTime)/1000;
      fill(100);
      rect(x,y,w,h);
      fill(255);
      text("Hold for " + (5 - heldSeconds) + " seconds to erase movie...", x+20,y+20,w-40,h-40);
      //if(!keyPressed || key!='r') resetRequesting= false;
      if(5 - heldSeconds <= 0){
        animation.clearAnimation();
        resetRequesting= false;
        lastReset=millis();
      }
      
    }
  }
  
  public void stopRequest(){
    resetRequesting= false;
  }
  
  public void requestReset(){
    if(!resetRequesting && millis() > lastReset+2000){
      requestStartTime= millis();
      resetRequesting= true;
    }
  }
}
  
class ScrollBar{
  int x, y, w, h;
  ScrollBar(int _x, int _y, int _w, int _h){
    x=_x;
    y=_y;
    w=_w;
    h=_h;
  }
  
  public void update(){
    drawTrack();
    drawDoober();
  }
  
  public void drawTrack(){
    noStroke();
    fill(50);
    ellipseMode(CORNER);
    ellipse(x,y,h,h);
    ellipse(x+w-h,y,h,h);
    rect(x+h/2,y,w-h,h);
  }  
  
  public void drawDoober(){
    // max doober width is track width, minimum is track height (cause it's a circle)
    float dooberWidth;
    if(animation.numFrames < filmstrip.numVisibleFrames) dooberWidth= w;
    else dooberWidth= (PApplet.parseFloat(filmstrip.numVisibleFrames) / animation.numFrames) * w;
    
    float leftSpace= map(floor(animation.playhead), 0, animation.numFrames+1, 0, w-dooberWidth);
    
    fill(100);
    // plus and minus 2's and 4's to inset slider in track a bit
    ellipse(x+leftSpace+2, y+2, h-4, h-4);
    ellipse(x+leftSpace+dooberWidth-h+2, y+2, h-4, h-4);
    rect(x+leftSpace+(h)/2+2, y+2, dooberWidth-h-4, h-4);
  }
  
  public void drawSlider(){
  }
}
class Viewer{
  PImage viewerImage, emptyFrame;
  int x,y,w,h; // x & y positions, width, and height
  
  int captureH= height-height/3;
  int captureW= (captureH*640)/480;
  int captureX= width/2-captureW/2;
  int captureY= filmstrip.captureY/2-captureH/2;
  
  int scrubW= 640;
  int scrubH= 480;
  int scrubX= width/2-scrubW/2;
  int scrubY= (height-height*5/16)/2-240;
 
  
  boolean scrubbing= false;
  int lastScrubTime= 0;
  
  Viewer(){
    x= captureX;
    y= captureY;
    w= captureW;
    h= captureH;
    
    emptyFrame= animation.emptyFrame;
    viewerImage= emptyFrame;
  }
  
  public void update(){
    if(!scrubbing){
      x= captureX;
      y= captureY;
      w= captureW;
      h= captureH;
    }
    else{
      x= scrubX;
      y= scrubY;
      w= scrubW;
      h= scrubH;
    } 
    
    if(scrubbing) drawPlayhead();
    
    if(scrubbing){
      viewerImage= animation.getFrame(floor(animation.playhead)-1).get();
      viewerImage= animation.maskFrame(viewerImage);
    }
    else viewerImage= camImage;
    
    if(filmstrip.frameBeingAdded) tint(255, filmstrip.adderProgress*255);
    image(viewerImage, x,y,w,h);
    
    // oninon skin last frame here:
    if(!scrubbing && !filmstrip.frameBeingAdded && animation.numFrames > 1){
      tint(255, 127);
      image(animation.getFrame(floor(animation.playhead)-1), x,y,w,h);
    }
    noTint();
  }
  
  public void drawPlayhead(){
    noFill();
    smooth();
    strokeWeight(3);
    stroke(127);
    rect(filmstrip.w/2-filmstrip.frameWidth/2, filmstrip.y, filmstrip.frameWidth, filmstrip.h);
    //line(filmstrip.w/2-filmstrip.frameWidth/2, filmstrip.y, x, y);
    //line(filmstrip.w/2+filmstrip.frameWidth/2, filmstrip.y, x+w, y);
    //line(filmstrip.w/2-filmstrip.frameWidth/2, filmstrip.y+filmstrip.h, x, y+h);
    //line(filmstrip.w/2+filmstrip.frameWidth/2, filmstrip.y+filmstrip.h, x+w, y+h);
  }

}
  
    
  
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "make_shop_stop_motion" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
