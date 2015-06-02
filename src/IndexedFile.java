/**
 * This is Project 3 for CS310-001 created by Sherif Sarhan on 4/30/2015.
 */
public class IndexedFile
{
    private Disk disk;             // disk on which the file will be written
    private char[] buffer;         // disk buffer
    private int recordSize;        // in characters
    private int keySize;           // in characters
    private int indexRecordSize;       // in characters
    // fields describing data portion of file
    private int recordsPerSector;  // sectorSize/recordSize
    private int firstAllocated;    // sector number where data begins
    private int sectorsAllocated;  // sectors originally allocated for data
    private int overflowStart;     // sector number where overflow begins
    private int overflowSectors;   // count of overflow sectors in use
    // fields describing index portion of file
    private int indexStart;        // sector number where index begins
    private int indexSectors;      // number of sectors allocated for index
    private int indexRoot;         // sector number of root of index
    private int indexLevels;       // number of levels of index

    public IndexedFile(Disk disk, int recordSize, int keySize, int
            indexRecordSize, int firstAllocated, int indexStart,
                       int indexSectors, int indexRoot, int indexLevels)
    {
        this.disk = disk;
        this.recordSize = recordSize;
        this.keySize = keySize;
        this.indexRecordSize = indexRecordSize;
        this.firstAllocated = firstAllocated;
        this.indexStart = indexStart;
        this.indexSectors = indexSectors;
        this.indexRoot = indexRoot;
        this.indexLevels = indexLevels;
    }
    //will take an entire record as a parameter. The first several
    //characters (as determined by keySize) are the key.
    //will return false and not perform an insert if it finds that there
    //is already a record in the file with the given key.
    public boolean insertRecord(char[] record)
    {
        char[] key = new char[keySize];
        //extracts key from record
        int i;
        for (i=0;i<keySize;i++)
        {
            key[i] = record[i];
        }

        //checks to see if record already exists on the disk
        if(findRecord(record))
        {
            return false;
        }
        int sectorNum = getSector(key);
        char[] sector = new char[disk.getSectorSize()];
        disk.readSector(sectorNum, sector);
        int emptySpaceCount = 0;
        int recordIncr = recordSize;

        int startingSpot = 0;
        for (i=0;i<disk.getSectorSize();i++)
        {
            if (sector[i] == (char)0)
            {
                if(emptySpaceCount == 0)
                {
                    startingSpot = i;
                }
                emptySpaceCount++;
                if(emptySpaceCount == recordSize)
                {
                    break;
                }
            }

            else
            {
                recordIncr+= recordSize;
                emptySpaceCount = 0;
                startingSpot = 0;
                i = recordIncr-1;
            }
        }

        if(emptySpaceCount != recordSize)
        {
            startingSpot = -1;
        }

        recordIncr = 0;
        //if there is no room left on the sector, go to overflow
        if(startingSpot == -1)
        {
            if(!(overflowSectors > 0))
            {
                overflowStart = indexRoot+1;

                overflowSectors++;
            }
            emptySpaceCount = 0;
            int z;
            outerLoop:
            for (z=0; z<overflowSectors;z++)
            {
                disk.readSector(overflowStart+z, sector);
                sectorNum = overflowStart+z;
                //go to overflow
                for (i = 0; i < disk.getSectorSize(); i++)
                {
                    if (sector[i] == (char)0)
                    {
                        if(emptySpaceCount == 0)
                        {
                            startingSpot = i;
                        }
                        emptySpaceCount++;
                        if(emptySpaceCount == recordSize)
                        {
                            break;
                        }
                    }

                    else
                    {
                        recordIncr+= recordSize;
                        emptySpaceCount = 0;
                        startingSpot = 0;
                        i = recordIncr-1;
                    }
                }
            }
        }

        if(emptySpaceCount != recordSize)
        {
            return false;
        }

        //copy the buffer from the sector and add the record to it.
        for (i=0;i<recordSize;i++)
        {
            sector[i+startingSpot] = record[i];
        }

        disk.writeSector(sectorNum, sector);

        //placeholder
        return true;
    }
    //will take a character array the length of a record as a parameter.
    //The first keySize characters of this array will hold the key of
    //the record being searched for. findRecord will then overwrite
    //this array with the entire record if it is found in the file.
    //will return false if the record in the file with the given key
    //cannot be located. Will return true if successful.
    public boolean findRecord(char[] record)
    {
        //get the sector for which the record should belong
        int sectorNum = getSector(record);
        //read the data sector
        char[] sector = new char[disk.getSectorSize()];
        disk.readSector(sectorNum, sector);

        this.recordsPerSector = disk.getSectorSize()/recordSize;

        char[] eachKey;
        //look through each record of the sector and try to find a match
        int i;
        //checking first the sector where it should be
        for (i=0; i<recordsPerSector;i++)
        {
            eachKey = getKeyFromDataSector(i, sector);
            //compare target key with current record in the sector
            int comp = compareTo(record, eachKey);
            if (comp == 0)
            {
                int z;
                for (z=0;z<recordSize;z++)
                {
                    record[z] = sector[z+(recordSize*i)];
                }
                return true;
            }
        }

        //couldn't find it, check the overflow sectors
        //for each overflow sector
        int x;
        for (x = 0; x<overflowSectors; x++)
        {
            disk.readSector(overflowStart+x, sector);
            //for each record
            for (i=0; i<recordsPerSector;i++)
            {
                eachKey = getKeyFromDataSector(i, sector);
                //compare target key with current record in the sector
                int comp = compareTo(record, eachKey);
                if (comp == 0)
                {
                    int z;
                    for (z=0;z<recordSize;z++)
                    {
                        record[z] = sector[z+(recordSize*i)];
                    }
                    return true;
                }
            }
        }

        //failed to find anything
        return false;

    }

    private int getSector(char[] key)   // returns sector number indicated by key
    {
        final int expectedIndexRecordsPerSector = (disk.getSectorSize()/indexRecordSize);
        int indexRecordsPerSector;

        //start at root
        //get the root sector
        char[] currSector = new char[disk.getSectorSize()];
        int currSectorNum = indexRoot;
        char[] key2;
        int keyComp;
        int i;
        for (i = 0; i<indexLevels; i++)
        {
            disk.readSector(currSectorNum, currSector);

            indexRecordsPerSector = getRecordsCountFromSector(currSector, expectedIndexRecordsPerSector);

            //loop through the records in the sector
            int z;
            for(z=0; z<indexRecordsPerSector-1; z++)
            {
                //get the second key in the root sector
                key2 = getKey(z+1, currSector);

                //compare target key with "2nd" key in sector
                keyComp = compareTo(key,key2);

                //if target is less than 2nd key,go to previous key's sector
                if (keyComp<0)
                {
                    //get previous key's sector number
                    char[] prevKeySectorNum = getSectorFromRecordNumber(z,currSector);
                    currSectorNum = sectorArrayToInt(prevKeySectorNum);
                    //read the sector from the disk

                    disk.readSector(currSectorNum, currSector);
                    //repeat process for lower levels of index
                    break;
                }

                //if target is more than 2nd key, see if there is a third key.
                if (keyComp>0)
                {
                    //do nothing, move along rest of keys
                }

                if (keyComp==0 || z == indexRecordsPerSector-2)
                {
                    //get the second key's address
                    currSectorNum = sectorArrayToInt(getSectorFromRecordNumber(z+1,currSector));
                    disk.readSector(currSectorNum, currSector);
                    break;

                }
            }
        }
        return currSectorNum;
    }

    private int compareTo(char[] key1, char[] key2)
    {
        int i = 0;
        while(i<keySize)
        {
            if (charValue(key1[i])<charValue(key2[i]))
            {
                return -1;
            }
            if (charValue(key1[i])==charValue(key2[i]))
            {
                String key1Str = new String(key1);
                try
                {
                    key1Str = key1Str.substring(0, key1Str.indexOf((char)0));
                }
                catch (IndexOutOfBoundsException e)
                {
                    key1Str = key1Str.substring(0, keySize);
                }

                String key2Str = new String(key2);
                try
                {
                    key2Str = key2Str.substring(0, key2Str.indexOf((char)0));
                }
                catch (IndexOutOfBoundsException e)
                {
                    key2Str = key2Str.substring(0, keySize);
                }

                int key1Length = key1Str.length();
                int key2Length = key2Str.length();

                if(i+1==key1Length || i+1 == key2Length)
                {
                    if(key1Length < key2Length)
                    {
                        return -1;
                    }
                    else if(key1Length>key2Length)
                    {
                        return 1;
                    }
                    else if(key1Length == key2Length)
                    {
                        return 0;
                    }
                }
            }
            if (charValue(key1[i])>charValue(key2[i]))
            {
                return 1;
            }
            i++;
        }
        //operation failed
        return -2;
    }

    //keyRecordNumber is the record number of where the desired key lies. counting starts at 0.
    //must know which sector the key is in. Index sectors only
    private char[] getKey(int keyRecordNumber, char[] sector)
    {
        int offsetRecord = (keyRecordNumber)*(indexRecordSize);
        char[] key = new char[keySize];
        int i;
        for (i = 0; i < keySize; i++)
        {
            key[i] = sector[i+offsetRecord];
        }
        return key;
    }

    //same as getKey except it is for Data sectors only
    private char[] getKeyFromDataSector(int keyRecordNumber, char[] sector)
    {
        int offsetRecord = (keyRecordNumber)*(recordSize);
        char[] key = new char[keySize];
        int i;
        for (i = 0; i < keySize; i++)
        {
            key[i] = sector[i+offsetRecord];
        }
        return key;
    }

    //given a sector and a record number, find its corresponding subsector
    private char[] getSectorFromRecordNumber(int recordNum, char[] sector)
    {
        int offsetRecord = (recordNum*indexRecordSize)+(keySize);
        char[] subSector = new char[indexRecordSize-keySize];
        int i;
        for(i=0; i<indexRecordSize-keySize; i++)
        {
            subSector[i] = sector[i+offsetRecord];
        }
        return subSector;
    }

    //turns sector address array to int
    private int sectorArrayToInt(char[] sectorArray)
    {
        String sectorStr = new String(sectorArray);
        try
        {
            sectorStr = sectorStr.substring(0,sectorStr.indexOf((char) 0));
        }
        catch (NullPointerException e)
        {
            sectorStr = sectorStr.substring(0,keySize);
        }

        int sectorInt = Integer.parseInt(sectorStr);
        return sectorInt;
    }

    private int charValue(char ch)
    {
        int value = Character.valueOf(ch);
        //adjust lower to uppercase
        if(value>=97 && value<=122)
        {
            value-=32;
        }
        return value;
    }

    //returns the number of records of an index sector
    private int getRecordsCountFromSector(char[] sector, int maxRecords)
    {
        char[] key;
        int i;
        int recordCount = 0;
        for (i=0;i<maxRecords;i++)
        {
            key = getKey(i, sector);
            if(!(key[0]==(char)0))
            {
                recordCount++;
            }
            else
            {
                break;
            }
        }
        return recordCount;
    }
}
