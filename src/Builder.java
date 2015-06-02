/**
 * This is Project 3 for CS310-001 created by Sherif Sarhan on 4/30/2015.
 */

import java.io.*;
import java.util.*;

public class Builder
{
    //returns the numbers of the first and last sectors allocated for the data
    public static int[] buildData(Disk disk)
    {
        //open file, build records, write them into consecutive disk sectors
        //starting at sector 1000.
        //when you initially build the data sectors, leave space in each
        //sector for three additional records
        File file = new File("./mountains.txt");
        try
        {
            //scans file
            Scanner scanner = new Scanner(file);
            //keeps track of how many records are inserted into buffer
            int recInsCount = 0;
            int startSector = 1000;
            //keeps track of how many sectors have been used
            int sector = 1000;
            String buffer = "";
            //scans each line in file
            while (scanner.hasNextLine())
            {
                String line = scanner.nextLine();
                //creates a record from the line
                String record = dataRecordFromLine(line);

                //makes sure that only 5 records are inserted into sector
                //since we must leave room for 3 additional records in a sector
                //each sector can store 8 records. (512chars per sector > 480 (60chars*8records))
                if(recInsCount<5)
                {
                    //creates a buffer from the 5 records
                    buffer = buffer.concat(record);
                    recInsCount++;
                }
                else
                {
                    //writes the buffer to a sector on the disk
                    disk.writeSector(sector, buffer.toCharArray());

                    //resets the buffer for re-use
                    buffer = "";
                    buffer = buffer.concat(record);
                    recInsCount = 1;
                    sector++;
                }
            }
            if (!buffer.isEmpty() && recInsCount<5)
            {
                //writes the buffer to a sector on the disk
                disk.writeSector(sector, buffer.toCharArray());
            }
            //done fetching the data and storing it into sectors
            //return the numbers of the first and last sectors allocated for the data
            return new int[]{startSector,sector};

        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String dataRecordFromLine(String line)
    {
        //checks if the line is empty
        if (line.isEmpty())
        {
            return null;
        }

        char[] name = new char[27];
        int poundLocation = 0;
        int i;
        //loops through all the characters in the line
        for (i=0;i<line.length();i++)
        {
            //if the current character is a hash/pound
            if(line.charAt(i) == '#')
            {
                //see if current i is less than 27
                if(i<27)
                {
                    //if so, then set null terminator
                    name[i] = (char)0;
                }
                //set the location of the pound sign
                poundLocation = i;
                break;
            }
            //if not a pound, see if i is less than 27
            else
            {
                if(i<27)
                {
                    //store the char into the name string
                    name[i] = line.charAt(i);
                }
            }
        }
        char[] country = new char[27];
        //loop through all characters in line starting from after the pound symbol
        for (i=poundLocation+1;i<line.length();i++)
        {
            //see if current character is a pound sign
            if(line.charAt(i) == '#')
            {
                //if number of characters stored in country is less than 27
                if(i-poundLocation-1 <27)
                {
                    //set null terminator in country string
                    country[i-poundLocation-1] = (char)0;
                }
                //set location of pound symbol
                poundLocation = i;
                break;
            }
            //if not a pound sign
            else
            {
                //check if num of chars in country string is less than 27
                if(i-poundLocation-1<27)
                {
                    //store the char in the country string
                    country[i-poundLocation-1] = line.charAt(i);
                }
            }
        }
        char[] altitude = new char[6];
        //loops from last pound location +1 until 6 characters after
        for (i=poundLocation+1;i<poundLocation+6;i++)
        {
            //if you have reached the max length of the line
            if(i == line.length())
            {
                //set null terminator
                altitude[i-poundLocation-1] = (char)0;
                break;
            }
            //store current character in altitude string
            altitude[i-poundLocation-1] = line.charAt(i);
        }
        return new String(name) + new String(country) + new String(altitude);
    }

    public static int[] buildIndex(Disk disk, int firstSectorAllocated, int lastSectorAllocated, int indexLevels)
    {
        int[] iPackage = new int[2];
        char[] sector = new char[disk.getSectorSize()];
        //the first sector for index to be allocated in
        //when incremented, will be used as the current sector that is being written to
        //will eventually become the number of the last sector allocated
        int sectorNum = lastSectorAllocated+1;
        String sectorStr;
        String key;
        String indexRecord;
        String buffer = "";
        String iString;
        int indexRecsStoredInBuffer = 0;
        int i;
        //loop through the previous level's sectors
        for(i=firstSectorAllocated;i<lastSectorAllocated+1;i++)
        {
            //read each sector from disk
            disk.readSector(i,sector);
            //convert sector into string
            sectorStr = new String(sector);
            //pull in key from each record in sector
            key = sectorStr.substring(0,27);
            //if the sector number length is less than 7 characters, fill the rest out with null terminators
            iString = Integer.toString(i);
            if(iString.length()<7)
            {
                char[] iStringChars = new char[7];
                int z;
                for (z = 0; z<6;z++)
                {
                    if(z>=iString.length())
                    {
                        iStringChars[z] = (char)0;
                    }
                    else
                    {
                        iStringChars[z] = iString.charAt(z);
                    }
                }
                iStringChars[6] = (char)0;
                iString = new String((iStringChars));
            }
            //store key and sector number of key together in one index record
            indexRecord = key.concat(iString);

            if (indexRecsStoredInBuffer < 14)
            {
                buffer = buffer.concat(indexRecord);
                indexRecsStoredInBuffer++;
            }
            else
            {
                disk.writeSector(sectorNum,buffer.toCharArray());
                buffer = "";
                buffer = buffer.concat(indexRecord);
                indexRecsStoredInBuffer = 0;
                sectorNum++;
            }
        }
        if (!buffer.isEmpty())
        {
            disk.writeSector(sectorNum,buffer.toCharArray());
        }

        if(!(sectorNum-lastSectorAllocated == 1))
        {
            indexLevels++;
            iPackage = buildIndex(disk, lastSectorAllocated+1, sectorNum, indexLevels);
        }
        if(sectorNum-lastSectorAllocated == 1)
        {
            indexLevels++;
            iPackage[0] = sectorNum;
            iPackage[1] = indexLevels;
        }

        //return rootSector;
        return iPackage;
    }
}
