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
  
  void update(){
    if(millis() < startTime+5000){
      if(msg != null) drawMessage();
    }
    else msg= null;
    
    
  }
  
  void setMessage(String _msg){
    msg= _msg;
    startTime= millis();
  }
  
  void drawMessage(){
      fill(100);
      rect(x,y,w,h);
      fill(255);
      if (msg != null) text(msg, x+20,y+20,w-40,h-40);
  }    
}
  
