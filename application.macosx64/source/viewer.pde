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
  
  void drawPlayhead(){
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
  
    
  
