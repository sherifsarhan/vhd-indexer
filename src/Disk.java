/**
 * This is Project 3 created by Sherif Sarhan on 4/21/2015
 * for Nordstrom's CS310-001.
 */
public class Disk
{
    private int sectorCount;   // sectors on the disk
    private int sectorSize;    // characters in a sector
    private char[][] store;    // all disk data is stored here
   public Disk()    // for default sectorCount and sectorSize
    {
        sectorCount = 10000;
        sectorSize = 512;
        store = new char[sectorCount][sectorSize];
    }
    public Disk(int sectorCount, int sectorSize)
    {
        this.sectorCount = sectorCount;
        this.sectorSize = sectorSize;
        store = new char[sectorCount][sectorSize];
    }
    //will copy the contents of the sector whose number is the first
    //parameter to the character array which is the second parameter.
    public void readSector(int sectorNumber, char[] buffer)   // sector to
                                                              // buffer
    {
        int i;
        for(i=0;i<buffer.length;i++)
        {
            buffer[i] = store[sectorNumber][i];
        }
    }
    //will copy the contents of its second parameter (which should be
    //your disk buffer (char[] buffer)) to the sector whose number is
    //the first argument. The previous contents of the sector will
    //be overwritten.
    public void writeSector(int sectorNumber, char[] buffer)  // buffer to
                                                              // sector
    {
        int i;
        for(i=0;i<buffer.length;i++)
        {
            store[sectorNumber][i] = buffer[i];
        }
    }

    public int getSectorCount()
    {
        return sectorCount;
    }
    public int getSectorSize()
    {
        return sectorSize;
    }
}
