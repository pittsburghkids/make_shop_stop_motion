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
  
  void playMovie(){
    playing= true;
    currentFrameStart= millis();
    currentFrame= 0;
  }
  
  void update(){
    image(animation.animation[currentFrame], 0, 0, width, height);
    if(millis() > currentFrameStart+msPerFrame){
      currentFrame++;
      currentFrameStart=millis();
    }
    if(currentFrame >= animation.numFrames) currentFrame= 0;       
  }
  
  void stopPlaying(){
    playing= false;
  }
}

