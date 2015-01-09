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
  
  void update(){
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
  
  void stopRequest(){
    resetRequesting= false;
  }
  
  void requestReset(){
    if(!resetRequesting && millis() > lastReset+2000){
      requestStartTime= millis();
      resetRequesting= true;
    }
  }
}
  
