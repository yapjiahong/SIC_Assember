import java.util.*;
import java.io.*;

public class SICASM
{

/**
* @param args
*/
    static String filename1, filename2;  //filename input sic.asm filename2 output a immediate file
    static int nlineNo;//number of line
    static String Label, Opcode, Oprand, Line;
    static boolean IsCommentLine;
    static BufferedReader sr;//Soucre file
    static PrintWriter sw;//Outout file
    static int Location, LOCCTR;
    static HashMap<String, Integer> SYMTAB = new HashMap<String, Integer>(); // hash use for sysmtbol table
    static Properties OPCODE;
    /*SIC Instruction: 
     * ADD,AND,COMP,DIV,J,JEQ,JGT,JLT,JSUB,LDA,LDCH,LDL,LDX,MUL,OR,RD,RSUB,STA,STCH,STL,STX,SUB,TD,TIO,TIX,WD
    */
    static String[] OPTAB = new String[] { "ADD", "AND", "COMP", "DIV", "J", "JEQ", "JGT", "JLT", "JSUB", "LDA", "LDCH", "LDL", "LDX", "MUL", "OR", "RD", "RSUB", "STA", "STCH", "STL", "STX", "SUB", "TD", "TIO", "TIX", "WD" }; 
    public static void main(String[] args) {
// TODO Auto-generated method stub
    String ErrorMessage;
        
        
        pass_2(); //Doing Pass2
        
        hexobj();//Create a hex object code
}
    
    static boolean ReadFirstLine()
    {
        return ReadNextLine();
    }
    static boolean ReadNextLine()
    {
    try
        {
    Line = sr.readLine();
        }
        catch (IOException ie) {
        System.out.println(ie.getMessage()); 
        }
        if (Line == null) return false;

        if (Line.charAt(0) == '.')
        {
            IsCommentLine = true;
        }
        else
        {
            IsCommentLine = false;
            Label = Line.substring(0, 7).trim();
            Opcode = Line.substring(9, 16).trim(); ;
            if (Line.length() >= 36)
                Oprand = Line.substring(17, 33).trim();
            else
                Oprand = Line.substring(17).trim();
        }
        nlineNo = nlineNo + 5;
        return true;
    }
    static boolean SearchOPTAB(String opcode)
    {
        for (int i = 0; i < OPTAB.length; i++)
        {
            if (opcode.equals(OPTAB[i]))
            {
                return true;
            }
        }
        return false;
    }
    static int FindConstantLength(String oprand)
    {
        if (oprand.length() <3)
            return 0;
        int Q1, Q2;
        int nLen = 0;
        switch (oprand.charAt(0))
        {
            case 'C':
                Q1 = oprand.indexOf('\'');
                if (Q1 >= 0)
                {
                    Q2 = oprand.indexOf('\'', Q1+1);
                    if (Q2 > Q1)
                        nLen = Q2 - Q1 - 1;
                }
                break;
            case 'X':
                Q1 = oprand.indexOf('\'');
                if (Q1 >= 0)
                {
                    Q2 = oprand.indexOf('\'', Q1 + 1);
                    if (Q2 > Q1)
                        nLen = (Q2 - Q1 - 1)/2;
                }
                break;

        }
        return nLen;
    }
    static void WriteThisLine()
    {
        if (IsCommentLine)
        {
            sw.printf("%3d %s%n", nlineNo, Line);
            System.out.printf("%3d %s%n", nlineNo, Line);

        }

        else
        {   
        String Space ="                   ";
        String FormatLabel = Label + (Label.length()< 8 ? Space.substring(0, 9-Label.length()-1): Label.length());
        String FormatOpcode = Opcode + (Opcode.length()< 8 ? Space.substring(0, 9-Opcode.length()-1): Opcode.length());
        String FormatOprand = Oprand + (Oprand.length()< 8 ? Space.substring(0, 9-Oprand.length()-1): Oprand.length());
            sw.printf("%3d %4X %s %s %s%n", nlineNo, Location, FormatLabel, FormatOpcode, FormatOprand);
            System.out.printf("%3d %4X %s %s %s%n", nlineNo, Location, FormatLabel, FormatOpcode, FormatOprand);
        }
    }
    static void WriteLastLine()
    {
    sw.printf("%3d           %s%n", nlineNo, Opcode);
        System.out.printf("%3d           %s%n", nlineNo, Opcode);
    }
    
    static void printsysmboltable() throws IOException
    {
    	FileWriter fw = new FileWriter("sysmboltable.txt");
    	System.out.println("Symbol\tAddrees\t");
    
    	for(Object key:SYMTAB.keySet())
    	{
    		System.out.println(key + " = "+SYMTAB.get(key));
    		fw.write(key+" "+SYMTAB.get(key)+System.getProperty("line.separator"));
    		//fw.write("\n");
    	}
    	
		fw.flush();
		fw.close();
    }
    
    //***************** PASS2 *************************************
	private static void pass_2()
	{
		String filename = "output.txt";
		String opfilename = "sic.op";
		String objfilename = "output.obj";
		
		String label, op, arg,start,address;
		int line = 0;
		
		OPCODE = new Properties();//opcode number to a file to search it
		
		try {
			FileReader opfilereader = new FileReader("sic.op");
			FileWriter objwriter = new FileWriter("output.obj");
			BufferedReader src = new BufferedReader(new FileReader("output.txt"));
			
			OPCODE = new Properties();
			OPCODE.load(opfilereader);
			
			System.out.println("\nOPTAB generated sucessfuly.\n");
			System.out.println("Opcode"+"  "+"Code");
			for(Object key:OPCODE.keySet())
			{
				//String value = OPCODE.getProperty((String) key);
				System.out.println(key+" = "+OPCODE.getProperty((String) key));
			}
			
			System.out.println("Initializing tokenizer object to parse out text.");
			StringTokenizer stline;
			
			// initializing internal counters

			int rl = 0; // lines read
			int wl = 0; // lines written
			int blen = 0; // byte length of the program
			int e =0; // errors
			
			// each T record can have at most 10 instruction records
			int crec = 0;
			
			
			String inline = src.readLine();
			stline = new StringTokenizer(inline);
			
			String tt = stline.nextToken();
			
			if(stline.countTokens()<4)
			{
				line = Integer.valueOf(tt);
				address = stline.nextToken();
				label = "";
				op = stline.nextToken();
				if(stline.hasMoreTokens())
					arg = stline.nextToken();
				else
					arg = null;
			}
			else
			{
				line = Integer.valueOf(tt);
				address = stline.nextToken();
				label = stline.nextToken();
				op = stline.nextToken();

				if(stline.hasMoreTokens())
					arg = stline.nextToken();
				else
					arg = null;
			}

			// check if the first line has start
			if(op.toUpperCase().equals("START"))
			{
				System.out.println("Found START directive on line " + rl);
				inline = src.readLine();
				stline = new StringTokenizer(inline);
				rl++;
			}
			else
			{
				System.out.println("No START directive found. Using 0 as start.");
				op = "0";
				label ="";
				arg ="0";

				System.out.println("Error on line 1: no START directive found!");
				e++;
			}			
			
			String operand, code ="";
			stline = new StringTokenizer(inline);
			tt = stline.nextToken();
			String checkdock = "";
			
			objwriter.write(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+System.getProperty("line.separator"));
			System.out.println(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+System.getProperty("line.separator"));			
			
			String ttemp= null;
			// the MAIN LOOP
			while(true)
			{
				System.out.println("-- Parsing line " + rl);
				int objcode = 0;
				boolean ascii = false;
				
				if(stline.countTokens()<4)
				{
					line = Integer.valueOf(tt);
					checkdock = String.valueOf(tt);
					if(stline.hasMoreTokens())
						address = stline.nextToken();
					else
						address = null;
					label = "";
					if(stline.hasMoreTokens())
						op = stline.nextToken();
					else
						op = null;
					if(stline.hasMoreTokens())
						arg = stline.nextToken();
					else
						arg = null;
				}
				else
				{
					line = Integer.valueOf(tt);
					address = stline.nextToken();
					label = stline.nextToken();
					op = stline.nextToken();

					if(stline.hasMoreTokens())
						arg = stline.nextToken();
					else
						arg = null;
				}

				if(address.toUpperCase().equals("END"))
				{
					objwriter.write(line+"\t\t\t"+address+System.getProperty("line.separator"));	
					break;
				}
				if(!address.equals("."))
				{
					System.out.println("Line " + rl + " is not a comment..");

					if(OPCODE.containsKey(op))
					{
						System.out.println("Opcode " + op + " found in OPTAB");
						System.out.println("\t\t\t\t\tOP\t" + op + " = " + OPCODE.get(op));
						String temp = (String) OPCODE.get(op);
						int temp2 = Integer.parseInt(temp, 16); 
						temp2 = temp2 *65536;
						objcode = objcode + temp2;
						
						String[] splitX = null;
						if(arg!=null)
						{
							splitX = arg.split(",");
							arg = splitX[0];
						}
						else
						{
							;
						}
						if(arg != null && SYMTAB.containsKey(arg))
						{
							
							char regX;
							boolean xFlag = false;
							if(splitX.length==2)
							{
								 regX = splitX[1].charAt(0);
								 xFlag = true;
							}
							else
								 xFlag = false;
							if(xFlag == false)
							{
								System.out.println("Symbol " + arg + " found in SYMTAB");
								System.out.println("\t\t\t\t\tSYM\t" + arg + " = " + SYMTAB.get(arg));
								temp = String.valueOf(SYMTAB.get(arg));
								temp2 = Integer.parseInt(temp,10);
								objcode = objcode + temp2;
							}
							if(xFlag == true)
							{
								System.out.println("Symbol " + arg + " found in SYMTAB");
								System.out.println("\t\t\t\t\tSYM\t" + arg + " = " + SYMTAB.get(arg));
								temp = String.valueOf(SYMTAB.get(arg));
								temp2 = Integer.parseInt(temp,10);
								objcode = objcode + temp2+32768;
								arg = arg + ",X";
							}
						}
						else
						{
							if(op.equals("RSUB"))
							{
								arg = "";
							}
							
							System.out.println("Error in line " + rl + " - Undefined Symbol");
							
							e++;
						}
					}
					else
					{
						System.out.println("Opcode " + op + " not found");
						System.out.println("\t\t\t\t\tOP\t" + op);

							if(arg.charAt(0)=='C')
							{
								String s = "";
								for(int i = 2;i<arg.length();i++)
								{
									if(arg.charAt(i)!='\'')
									{
										int itemp = 0;
										itemp =  + (int)arg.charAt(i);
										s = s +Integer.toUnsignedString(itemp,16);
									
									}
								}
								//objcode = Integer.valueOf(s);
								ascii = true;
								System.out.printf(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+s+System.getProperty("line.separator"));					
								objwriter.write(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+s+System.getProperty("line.separator"));		
							}
							if(arg.charAt(0)=='X')
							{
								String s = "";
								for(int i = 2;i<arg.length();i++)
								{
									if(arg.charAt(i)!='\'')
									{
										s = s + arg.charAt(i);
									}
								}
								//objcode = Integer.valueOf(s);
								ascii = true;
								System.out.printf(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+s+System.getProperty("line.separator"));					
								objwriter.write(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+s+System.getProperty("line.separator"));		
							}
							if(op.equals("WORD"))
							{
								int i = Integer.valueOf(arg,10);
								objcode =objcode + i;
							}
							/*
							if(arg.charAt(0)=='X')
							{
								int i = Integer.valueOf(arg,16);
								objcode = objcode + i;
							}
							*/
							if(op.equals("RESW"))
							{
								ascii = true;
								System.out.printf(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+"\t"+System.getProperty("line.separator"));					
								objwriter.write(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+"\t"+System.getProperty("line.separator"));								
							}
							if(op.equals("RESB"))
							{
								ascii = true;
								System.out.printf(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+""+System.getProperty("line.separator"));					
								objwriter.write(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+"\t"+System.getProperty("line.separator"));
							}
							/*
							if(op.equals("BYTE"))
							{
								ascii = true;
								String s = "";
								for(int i = 2;i<arg.length();i++)
								{
									if(arg.charAt(i)!='\'')
									{
										s =s +arg.charAt(i);
									
									}
								}
								//objcode = Integer.valueOf(s);
								
								System.out.printf(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+s+System.getProperty("line.separator"));					
								objwriter.write(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+s+System.getProperty("line.separator"));	
							}*/
													
					}
					/*
					if(crec <=10)
					{
						// add the object code to the first record
						code += ((String) OPCODE.get(op)) + operand;
						crec++;

						//debug("code: " + code);
					}
					else
					{
						o.writeBytes(code + "\n");
						crec=0;
						code="";
					}
					*/
					if(ascii==false)
					{
						String hex = Integer.toHexString(objcode);
						String addZero = "";
						if(op.equals("LDA"))
						{
							addZero = "00";
							addZero = addZero + hex;
							hex = addZero;
						}
						if(op.equals("LDL")||op.equals("LDX")||op.equals("STA"))
						{
							addZero = "0";
							addZero = addZero + hex;
							hex = addZero;
						}
						if(op.equals("WORD"))
						{
							addZero = "000000";
							int i = 0;
							for(;i<hex.length();i++)
							{
								;
							}
							addZero = addZero + hex;
							hex = addZero.substring(i, 5+i+1);
						}
						System.out.printf(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+hex+System.getProperty("line.separator"));					
						objwriter.write(line+"\t"+address+"\t"+label+"\t"+op+"\t"+arg+"\t"+hex+System.getProperty("line.separator"));
					}
					System.out.println("Reading a line of the source file");
					String t = src.readLine();
					ttemp = t;
					//debug("**LINE:\t " + t);

					stline = new StringTokenizer(t);
					rl++;

					tt = stline.nextToken();
				}
				if(address.equals("."))
				{
					
					System.out.println(ttemp+System.getProperty("line.separator"));
					objwriter.write(ttemp+System.getProperty("line.separator"));
					
					System.out.println("Reading a line of the source file");
					String t = src.readLine();
	
					//debug("**LINE:\t " + t);

					stline = new StringTokenizer(t);
					rl++;
					ttemp = t;
					tt = stline.nextToken();
				}

			}
			
			
			//opfilereader.close();
			src.close();
			objwriter.close();
			
		}
		catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//************************************** Create Hex Objcode file ******************************
	private static void hexobj()
	{
		System.out.println();
		System.out.println("Try to Create Hex.obj");
		
		String label, op, arg,address,objcode = "";
		Integer noline;
		
		String inputName = "output.obj";
		String outputName = "hex.obj";
		
		try {
			BufferedReader src = new BufferedReader(new FileReader("output.obj"));
			FileWriter objwriter = new FileWriter(outputName);
			
			System.out.println("Opening the destination file...");
			
			String out;
			
			System.out.println("Initializing tokenizer object to parse out text.");
			StringTokenizer line;

			// initializing internal counters
			int rl = 0; // lines read
			int wl = 0; // lines written
			int blen = 0; // byte length of the program
			int e =0; // errors
			
			// number of records on the current line
			// each T record can have at most 10 instruction records
			int crec = 0;

			System.out.println("Reading the 1st line of the source file...");
			String inline = src.readLine();
			line = new StringTokenizer(inline);	// tokenized uses whitespace as delimiters

			rl++; // increment readline
			
			// this variable is used to temporarily hold the first token read on the
			// line. Since 1st token can be either a '.' (dot - comment), a label or an opcode
			// we need to evaluate it first before assigning it to either op or label
			String tmp;

			int next; // next line to read is ...

			String tt = line.nextToken();
			
			if(line.countTokens()<4)
			{
				noline = Integer.valueOf(tt);
				address = line.nextToken();
				label = "";
				op = line.nextToken();
				if(line.hasMoreTokens())
					arg = line.nextToken();
				else
					arg = null;
			}
			else
			{
				noline = Integer.valueOf(tt);
				address = line.nextToken();
				label = line.nextToken();
				op = line.nextToken();

				if(line.hasMoreTokens())
					arg = line.nextToken();
				else
					arg = null;
			}
			
			// check if the first line has start
			if(op.toUpperCase().equals("START"))
			{
				System.out.println("Found START directive on line " + rl);
				inline = src.readLine();
				line = new StringTokenizer(inline);
				rl++;
			}
			else
			{
				System.out.println("No START directive found. Using 0 as start.");
				op = "0";
				label ="";
				arg ="0";

				System.out.println("Error on line 1: no START directive found!");
				e++;
			}	
			
			//write the start to hex.obj
			
			//Header record
			System.out.println("Processing Header Record");
			String fristLable = "",secondLable = "";
			int temploc = LOCCTR;
			//temploc = temploc;
			String addZero = "000000";
			int StartingAddressindex = 0;
			for(StartingAddressindex = 0;StartingAddressindex<address.length();StartingAddressindex++)
			{
				;
			}
			int startaddress = Integer.valueOf(address,16);
			temploc = temploc - startaddress;
			addZero = addZero + address;
			address = addZero.substring(StartingAddressindex, 5+StartingAddressindex+1);
			
			addZero = "000000";
			StartingAddressindex = 0;
			String hexloc = Integer.toHexString(temploc);
			
			for(StartingAddressindex = 0;StartingAddressindex<hexloc.length();StartingAddressindex++)
			{
				;
			}
			addZero = addZero + hexloc;
			hexloc = addZero.substring(StartingAddressindex, 5+StartingAddressindex+1);
			String initialAdrees = address;
			fristLable = fristLable + "H^"+label+"\t^"+address+"^"+hexloc+System.getProperty("line.separator");
			System.out.println("Hearder Record:\t"+fristLable);
			objwriter.write(fristLable);
			
			String operand, code ="";
			line = new StringTokenizer(inline);
			tt = line.nextToken();
			String checkdock = "";
			String ttemp= null;
			// the MAIN LOOP
			while(true)
			{
				System.out.println("-- Parsing line " + rl);
				int linetaking = 0;
				String textStartingAddr = "";
				objcode = "";
				int textsize = 0;
				for(linetaking = 0;linetaking<7;linetaking++)
				{
					
					if(line.countTokens()==5)
					{
						noline = Integer.valueOf(tt);
						checkdock = String.valueOf(tt);
						if(line.hasMoreTokens())
							address = line.nextToken();
						else
							address = "";
						if(line.hasMoreTokens())
							label = line.nextToken();
						else
							label = "";
						if(line.hasMoreTokens())
							op = line.nextToken();
						else
							op = "";
						if(line.hasMoreTokens())
							arg = line.nextToken();
						else
							arg = "";
						
						if(line.hasMoreTokens())
						{
							if(op.equals("RESB")||op.equals("RESW"))
							{
								;
							}
							else
							{
							int tempindex = 0;
							String tempobjcode = line.nextToken();
							objcode = objcode +"^"+ tempobjcode;
							for(tempindex = 0;tempindex<tempobjcode.length();tempindex++)
							{
								;
							}
							textsize = textsize + (tempindex/2);
							}		
						}
						else
							objcode = "";
						
					}
					else if(line.countTokens()==4)
					{
						noline = Integer.valueOf(tt);
						address = line.nextToken();
						label = "";
						op = line.nextToken();

						if(line.hasMoreTokens())
							arg = line.nextToken();
						else
							arg = "";
						if(op.equals("RESW")||op.equals("RESB")||arg.equals("RESW")||arg.equals("RESB"))
							;
						else
						{
							//objcode = objcode +"^"+ line.nextToken();
							int tempindex = 0;
							String tempobjcode = line.nextToken();
							objcode = objcode +"^"+ tempobjcode;
							for(tempindex = 0;tempindex<=tempobjcode.length();tempindex++)
							{
								;
							}
							textsize = textsize + (tempindex/2);
						}
					}
					else if(line.countTokens()==3)
					{
						noline = Integer.valueOf(tt);
						address = line.nextToken();
						label = "";
						op = line.nextToken();

						arg = "";
					
						if(line.hasMoreTokens())
						{		
							//objcode = objcode +"^"+ line.nextToken();
							int tempindex = 0;
							String tempobjcode = line.nextToken();
							objcode = objcode +"^"+ tempobjcode;
							for(tempindex = 0;tempindex<=tempobjcode.length();tempindex++)
							{
								;
							}
							textsize = textsize + (tempindex/2);
						}
						else
							objcode = "";
					}
					else
					{
						noline = Integer.valueOf(tt);
						address = line.nextToken();
					}
					
					if(linetaking == 0)
						textStartingAddr = address;
					if(address.toUpperCase().equals("END"))
					{
						
						String sizeTextRecord = Integer.toHexString(textsize);
						String TextRecord = "T^"+textStartingAddr+"^"+sizeTextRecord+objcode+System.getProperty("line.separator");
						objwriter.write(TextRecord);
						System.out.println(TextRecord);
						objwriter.write("E^"+initialAdrees);
						break;
					}
					System.out.println("Reading a line of the source file");
					String t = src.readLine();
					System.out.println(t);
					//debug("**LINE:\t " + t);

					line = new StringTokenizer(t);
					rl++;

					tt = line.nextToken();
				}
				if(address.toUpperCase().equals("END"))
					break;
				String sizeTextRecord = Integer.toHexString(textsize);
				String TextRecord = "T^"+textStartingAddr+"^"+sizeTextRecord+objcode+System.getProperty("line.separator");
				objwriter.write(TextRecord);
				System.out.println(TextRecord);
			}
			src.close();
			objwriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
    
}