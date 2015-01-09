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



