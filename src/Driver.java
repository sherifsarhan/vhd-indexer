import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * This is Project 3 for CS310-001 created by Sherif Sarhan on 4/30/2015.
 */
public class Driver
{
    public static void main(String[] args)
    {
        int recordSize = 60;
        int keySize = 27;
        int indexRecordSize = 34;
        int firstAllocated = 1000;
        int indexStart;
        int indexRoot;
        int indexLevels;
        int indexSectors;
        //create disk
        Disk disk = new Disk();
        //build data
        int[] allocationValues = Builder.buildData(disk);
        indexStart = allocationValues[1] + 1;
        //build index
        int[] iPackage = Builder.buildIndex(disk, allocationValues[0], allocationValues[1], 0);

        indexRoot = iPackage[0];
        indexLevels = iPackage[1];
        indexSectors = indexRoot-indexStart;

        IndexedFile indexedFile = new IndexedFile(disk, recordSize, keySize, indexRecordSize, firstAllocated, indexStart, indexSectors, indexRoot, indexLevels);

        Scanner sc = new Scanner(System.in);
        String searchFor, insert, record;
        char[] recordChars;

        boolean quit = false;
        while(!quit)
        {
            System.out.println("Type 1 to Insert New Record");
            System.out.println("Type 2 to Find Record");
            System.out.println("Type 3 to Quit");

            int input = 3;
            boolean goodInput = false;
            while(!goodInput)
            {
                try
                {
                    System.out.print("\nSelect Option: ");
                    input = sc.nextInt();

                    goodInput = true;
                }
                catch (InputMismatchException e)
                {
                    sc.nextLine();
                    System.out.println("Please enter an integer.");
                    goodInput = false;
                }
            }
            sc.nextLine();

            switch (input)
            {
                case 1:
                    System.out.print("insert: ");
                    insert = sc.nextLine();
                    record = Builder.dataRecordFromLine(insert);
                    recordChars = record.toCharArray();
                    if(indexedFile.insertRecord(recordChars))
                    {
                        System.out.println("Insertion Successful\n");
                    }
                    else
                    {
                        System.out.println("Insertion Failed\n");
                    }
                    break;

                case 2:
                    System.out.print("search for: ");
                    searchFor = sc.nextLine();
                    record = Builder.dataRecordFromLine(searchFor);
                    recordChars = record.toCharArray();
                    if(indexedFile.findRecord(recordChars))
                    {
                        int i;
                        for (i=0;i<keySize;i++)
                        {
                            if(recordChars[i] == (char)0)
                            {
                                break;
                            }
                            System.out.print(recordChars[i]);
                        }
                        System.out.print(", country: ");
                        for(i=keySize;i<54;i++)
                        {
                            if(recordChars[i] == (char)0)
                            {
                                break;
                            }
                            System.out.print(recordChars[i]);
                        }
                        System.out.print(", altitude: ");
                        for (i=54;i<60;i++)
                        {
                            if(recordChars[i] == (char)0)
                            {
                                break;
                            }
                            System.out.print(recordChars[i]);
                        }
                        System.out.print(" ft.\n\n");
                    }
                    else
                    {
                        System.out.println("Record Not Found\n");
                    }
                    break;

                case 3:
                    System.out.println("Program Has Quit");
                    quit = true;
                    break;
                default:
                    System.out.println("Please Enter a Valid Option");
            }
        }
    }
}
