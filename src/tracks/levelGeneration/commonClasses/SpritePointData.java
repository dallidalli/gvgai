package tracks.levelGeneration.commonClasses;

/**
 * helpful data structure to hold information about certain points in the level
 * @author AhmedKhalifa
 */
public class SpritePointData implements Comparable{
    public String name;
    public int x;
    public int y;

    public SpritePointData(String name, int x, int y){
        this.name = name;
        this.x = x;
        this.y = y;
    }

    @Override
    public int compareTo(Object o) {
        try{
            SpritePointData p = (SpritePointData)o;

            int result;

            if(p.x == this.x && p.y == this.y){
                result = 0;
            } else if(p.x > this.x){
                result = -1;
            } else if(p.x < this.x){
                result = 1;
            }else {
                if(p.y > this.y){
                    result = -1;
                } else {
                    result = 1;
                }
            }

            if(result==0) {
                return p.name.compareTo(this.name);
            }
            else {
                return result;
            }

        }catch(ClassCastException e)
        {
            //Whatever this is, this is not a Pair. So, not equal at all.
            return -1;
        }

    }

    @Override
    public boolean equals(Object obj) {
        return (compareTo(obj)==0);
    }

    public boolean sameCoordinate(SpritePointData other){
        return (other.x == this.x && other.y == this.y);
    }

    @Override
    public String toString(){
        return "x = " + this.x + " y = " + this.y + "| name: " + this.name;
    }

    @Override
    public int hashCode() {
            int hash = 7;
            hash = 71 * hash + this.x;
            hash = 71 * hash + this.y;
            hash = 71 * hash + this.name.hashCode();
            return hash;
    }
}

