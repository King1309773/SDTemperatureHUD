package dev.rlcraftceaddons.sdtemperaturehud.map;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class MapLabelLayoutTest {
    private MapLabel label(double x,double y,int width,boolean priority) {
        return new MapLabel("a:b","Test",12,0,0,x,y,width,10,priority);
    }
    @Test public void circleMustContainAllCornersNotJustCenter() {
        MapLabel nearEdge=label(35,30,20,false);
        assertTrue(MapLabelLayout.layout(Arrays.asList(nearEdge),50,false,Collections.emptyList(),3).size()==1);
        assertTrue(MapLabelLayout.layout(Arrays.asList(nearEdge),50,true,Collections.emptyList(),3).isEmpty());
    }
    @Test public void removesOverlapsAndPrioritizesCurrentRegion() {
        MapLabel a=label(0,0,20,false),b=label(1,0,20,true),c=label(-35,0,10,false);
        List<MapLabel> labels=MapLabelLayout.layout(Arrays.asList(a,b,c),100,false,Collections.emptyList(),3);
        assertEquals(2,labels.size());assertSame(b,labels.get(0));assertSame(c,labels.get(1));
    }
    @Test public void limitsCountAndAvoidsControls() {
        List<MapLabel> candidates=new ArrayList<MapLabel>();
        for(int i=0;i<10;i++)candidates.add(label(i*20-90,0,8,false));
        assertEquals(3,MapLabelLayout.layout(candidates,200,false,Collections.emptyList(),3).size());
        MapLabelLayout.Rect control=new MapLabelLayout.Rect(-200,-20,400,40);
        assertTrue(MapLabelLayout.layout(candidates,200,false,Arrays.asList(control),40).isEmpty());
    }
    @Test public void fullMapCannotExceedFortyLabelsEvenWithALargerRequestedLimit() {
        List<MapLabel> candidates=new ArrayList<MapLabel>();
        for(int x=0;x<10;x++)for(int y=0;y<10;y++)candidates.add(label(x*20-90,y*20-90,8,false));
        assertEquals(40,MapLabelLayout.layout(candidates,200,false,Collections.emptyList(),100).size());
    }
}
