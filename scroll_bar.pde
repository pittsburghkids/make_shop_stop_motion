class ScrollBar{
  int x, y, w, h;
  ScrollBar(int _x, int _y, int _w, int _h){
    x=_x;
    y=_y;
    w=_w;
    h=_h;
  }
  
  void update(){
    drawTrack();
    drawDoober();
  }
  
  void drawTrack(){
    noStroke();
    fill(50);
    ellipseMode(CORNER);
    ellipse(x,y,h,h);
    ellipse(x+w-h,y,h,h);
    rect(x+h/2,y,w-h,h);
  }  
  
  void drawDoober(){
    // max doober width is track width, minimum is track height (cause it's a circle)
    float dooberWidth;
    if(animation.numFrames < filmstrip.numVisibleFrames) dooberWidth= w;
    else dooberWidth= (float(filmstrip.numVisibleFrames) / animation.numFrames) * w;
    
    float leftSpace= map(floor(animation.playhead), 0, animation.numFrames+1, 0, w-dooberWidth);
    
    fill(100);
    // plus and minus 2's and 4's to inset slider in track a bit
    ellipse(x+leftSpace+2, y+2, h-4, h-4);
    ellipse(x+leftSpace+dooberWidth-h+2, y+2, h-4, h-4);
    rect(x+leftSpace+(h)/2+2, y+2, dooberWidth-h-4, h-4);
  }
  
  void drawSlider(){
  }
}
