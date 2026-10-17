package dev.rlcraftceaddons.sdtemperaturehud.map;

import java.util.*;

public final class MapLabelLayout {
    private MapLabelLayout() {}
    public static final class Rect {
        public final double x,y,w,h;
        public Rect(double x,double y,double w,double h) {this.x=x;this.y=y;this.w=w;this.h=h;}
        public boolean contains(double px,double py) {return px>=x && px<=x+w && py>=y && py<=y+h;}
        public boolean intersects(Rect b) {return x<b.x+b.w && x+w>b.x && y<b.y+b.h && y+h>b.y;}
        public Rect expand(double gap) {return new Rect(x-gap,y-gap,w+gap*2,h+gap*2);}
    }
    public static List<MapLabel> layout(List<MapLabel> input,double radius,boolean circle,List<Rect> blocked,int limit) {
        return layout(input,new Rect(-radius,-radius,radius*2,radius*2),circle,blocked,limit);
    }
    public static List<MapLabel> layout(List<MapLabel> input,Rect viewport,boolean circle,List<Rect> blocked,int limit) {
        List<MapLabel> sorted=new ArrayList<MapLabel>(input);
        sorted.sort(Comparator.comparing((MapLabel label)->!label.priority)
            .thenComparingDouble(label->label.screenX*label.screenX+label.screenY*label.screenY)
            .thenComparing(label->label.registryId).thenComparingDouble(label->label.worldX)
            .thenComparingDouble(label->label.worldZ));
        List<MapLabel> placed=new ArrayList<MapLabel>();
        for(MapLabel label:sorted) {
            if(placed.size()>=Math.max(0,Math.min(40,limit))) break;
            Rect b=label.bounds();if(!fits(b,viewport,circle)) continue;
            boolean overlaps=false;
            for(Rect control:blocked) if(b.expand(2).intersects(control)) {overlaps=true;break;}
            for(MapLabel previous:placed) if(b.expand(2).intersects(previous.bounds())) {overlaps=true;break;}
            if(!overlaps) placed.add(label);
        }
        return Collections.unmodifiableList(placed);
    }
    private static boolean fits(Rect b,Rect view,boolean circle) {
        if(!view.contains(b.x,b.y) || !view.contains(b.x+b.w,b.y+b.h)) return false;
        if(!circle) return true;
        double cx=view.x+view.w/2,cy=view.y+view.h/2,radius=Math.min(view.w,view.h)/2;
        for(double x:new double[]{b.x,b.x+b.w})for(double y:new double[]{b.y,b.y+b.h})
            if((x-cx)*(x-cx)+(y-cy)*(y-cy)>radius*radius) return false;
        return true;
    }
}
