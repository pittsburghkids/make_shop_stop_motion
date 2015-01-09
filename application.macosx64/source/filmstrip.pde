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
    numVisibleFrames= ceil((float(w)/frameWidth))+2;
  }
  
  public void updateDisplay(){
    if(viewer.scrubbing){
          x= scrubX;
          y= scrubY;
          w= scrubW;
          h= scrubH;
              frameWidth= (h/3)*4;
                  numVisibleFrames= ceil((float(w)/frameWidth))+2;


        }
        else{
          x= captureX;
          y= captureY;
          w= captureW;
          h= captureH;
              frameWidth= (h/3)*4;
                  numVisibleFrames= ceil((float(w)/frameWidth))+2;


        }
        
    if(frameBeingAdded){
      adderProgress= map(millis(), lastFrameAddTime, lastFrameAddTime+frameAddDuration, 0, 1);
      if (adderProgress > 1.0) adderProgress= 1.0;

      animation.playhead=animation.numFrames+adderProgress;
      drawStrip(w-frameWidth/2);

      // float incoming frame towards strip...
      int floaterX= int(map(adderProgress, 0, 1, viewer.x, x+w-frameWidth));
      int floaterY= int(map(adderProgress, 0, 1, viewer.y, y));
      int floaterW= int(map(adderProgress, 0, 1, viewer.w, frameWidth));
      int floaterH= int(map(adderProgress, 0, 1, viewer.h, h));
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
      if (adderProgress > 1.0) adderProgress= 1.0;
      
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
    int offset= int((animation.playhead - floor(animation.playhead)) * frameWidth);
    
    PImage frame;
    
    // draw playhead frame...
    frame= animation.getFrame(floor(animation.playhead)-1).get();
    frame= animation.maskFrame(frame);
    if (frameBeingRemoved) tint(255, 255-255.0*adderProgress); // dim playhead frame if removing it
    image (frame, x+playheadPos-frameWidth/2-offset, y, frameWidth, h);
    
    // ...then, draw the frames to its left...
    int numFramesOnLeft= int((playheadPos/w)*(numVisibleFrames-1));
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
