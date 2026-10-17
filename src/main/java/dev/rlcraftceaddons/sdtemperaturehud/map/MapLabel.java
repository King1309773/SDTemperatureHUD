package dev.rlcraftceaddons.sdtemperaturehud.map;

public final class MapLabel {
    public final String registryId,name;
    public final int temperature,width,height;
    public final double worldX,worldZ,screenX,screenY;
    public final boolean priority;
    public MapLabel(String registryId,String name,int temperature,double worldX,double worldZ,
                    double screenX,double screenY,int width,int height,boolean priority) {
        if(temperature<0 || temperature>25 || width<=0 || height<=0
            || !Double.isFinite(screenX) || !Double.isFinite(screenY)) throw new IllegalArgumentException();
        this.registryId=registryId;this.name=name;this.temperature=temperature;
        this.worldX=worldX;this.worldZ=worldZ;this.screenX=screenX;this.screenY=screenY;
        this.width=width;this.height=height;this.priority=priority;
    }
    public MapLabelLayout.Rect bounds() {
        return new MapLabelLayout.Rect(screenX-width/2d,screenY-height/2d,width,height);
    }
}
