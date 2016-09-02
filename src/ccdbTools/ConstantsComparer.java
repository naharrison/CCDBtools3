package ccdbTools;

import java.util.Date;
import java.util.Vector;
import org.jlab.ccdb.Assignment;
import org.jlab.ccdb.CcdbPackage;
import org.jlab.ccdb.JDBCProvider;
import org.root.histogram.GraphErrors;

public class ConstantsComparer {

	// ------------------- Fields -------------------------------- //
	// ----------------------------------------------------------- //

	private String address;

	// ------------------ Constructors --------------------------- //
	// ----------------------------------------------------------- //

	public ConstantsComparer() {
		String envAddress = this.getEnvironment();
		
		if(envAddress == null)
		{
			System.out.println("DB address not defined in your environment.");
			System.out.println("Returning to default address: mysql://clas12reader@clasdb.jlab.org/clas12");
			this.address = "mysql://clas12reader@clasdb.jlab.org/clas12";
		}
		else
		{
			System.out.println(String.format("Using DB address from your environment: %s", envAddress));
			this.address = envAddress;
		}
	}

	public ConstantsComparer(String ADDRESS) {
		System.out.println(String.format("Address set to: %s", ADDRESS));
		this.address = ADDRESS;
	}

	// --------------------- Methods ----------------------------- //
	// ----------------------------------------------------------- //
	
    private String getEnvironment(){
        
        String envCCDB   = System.getenv("CCDB_DATABASE");
        String envCLAS12 = System.getenv("CLAS12DIR");
        
        String propCLAS12 = System.getProperty("CLAS12DIR");
        String propCCDB   = System.getProperty("CCDB_DATABASE");
        
        System.out.println("ENVIRONMENT : " + envCLAS12 + " " + envCCDB + " " + propCLAS12 + " " + propCCDB);
        
        if(envCCDB!=null&&envCLAS12!=null){
            StringBuilder str = new StringBuilder();
            str.append("sqlite:///");
            if(envCLAS12.charAt(0)!='/') str.append("/");
            str.append(envCLAS12);
            if(envCCDB.charAt(0)!='/' && envCLAS12.charAt(envCLAS12.length()-1)!='/'){
                str.append("/");
            }
            str.append(envCCDB);
            return str.toString();
        }
        
        if(propCCDB!=null&&propCLAS12!=null){
            StringBuilder str = new StringBuilder();
            str.append("sqlite:///");
            if(propCLAS12.charAt(0)!='/') str.append("/");
            str.append(propCLAS12);
            if(propCCDB.charAt(0)!='/' && propCLAS12.charAt(propCLAS12.length()-1)!='/'){
                str.append("/");
            }
            str.append(propCCDB);
            return str.toString();
        }
        
        return null;
    }
    
 	public String getAddress() {
		return address;
	}

	public void setAddress(String ADDRESS) {
		this.address = ADDRESS;
	}
    
	public void printTable(String tableName, String variationName, int runNumber, Date date){
		JDBCProvider provider = CcdbPackage.createProvider(this.address);
		provider.connect();
		if(provider.getIsConnected() != true) System.out.println("printTable error. Not connected.");
		else
		{
			provider.setDefaultVariation(variationName);
			provider.setDefaultRun(runNumber);
			provider.setDefaultDate(date);
			
			Assignment ass = provider.getData(tableName);
			int Ncolumns = ass.getColumnCount();
			int Nrows = ass.getRowCount();
			Vector<Vector<String>> tableValues = new Vector<>();
			
			for(int j = 0; j < Ncolumns; j++) tableValues.add(ass.getColumnValuesString(j));

			for(int k = 0; k < Nrows; k++)
			{
				for(int j = 0; j < Ncolumns; j++)
				{
					System.out.print(tableValues.get(j).get(k) + "   ");
				}
				System.out.println("");
			}
		}
		provider.close();
	}

	public double getConstantBySLC(String tableName, String variationName, int runNumber, Date date, int columnIndex, int sector, int layer, int component){
		double answer = -99;
		JDBCProvider provider = CcdbPackage.createProvider(this.address);
		provider.connect();
		if(provider.getIsConnected() != true) System.out.println("getConstantBySLC error. Not connected.");
		else
		{
			provider.setDefaultVariation(variationName);
			provider.setDefaultRun(runNumber);
			provider.setDefaultDate(date);
			
			Assignment ass = provider.getData(tableName);
			int Nrows = ass.getRowCount();	
			Vector<Integer> sectorVector = ass.getColumnValuesInt(0); // assuming first column is always sector
			Vector<Integer> layerVector = ass.getColumnValuesInt(1); // assuming second column is always layer
			Vector<Integer> componentVector = ass.getColumnValuesInt(2); // assuming second column is always component
			int rowIndex = -99;
			int loopCount = 0;
			while(rowIndex < -1 && loopCount < Nrows) {
				if(sectorVector.get(loopCount) == sector && layerVector.get(loopCount) == layer && componentVector.get(loopCount) == component) rowIndex = loopCount;
				loopCount++;
			}
			if(rowIndex > -1) answer = ass.getColumnValuesDouble(columnIndex).get(rowIndex);
		}
		provider.close();
		return answer;
	}
	
	public GraphErrors getConstantRunDependenceBySLC(String tableName, String variationName, int runStart, int runEnd, Date date, int columnIndex, int sector, int layer, int component) {
		GraphErrors answer = new GraphErrors();
		JDBCProvider provider = CcdbPackage.createProvider(this.address);
		provider.connect();
		if(provider.getIsConnected() != true) System.out.println("getConstantRunDependenceBySLC error. Not connected.");
		else
		{
			// find the row and column index of the requested constant:
			provider.setDefaultVariation(variationName);
			provider.setDefaultRun(runStart);
			provider.setDefaultDate(date);
			Assignment ass = provider.getData(tableName);
			int Nrows = ass.getRowCount();	
			Vector<Integer> sectorVector = ass.getColumnValuesInt(0); // assuming first column is always sector
			Vector<Integer> layerVector = ass.getColumnValuesInt(1); // assuming second column is always layer
			Vector<Integer> componentVector = ass.getColumnValuesInt(2); // assuming second column is always component
			int rowIndex = -99;
			int loopCount = 0;
			while(rowIndex < -1 && loopCount < Nrows) {
				if(sectorVector.get(loopCount) == sector && layerVector.get(loopCount) == layer && componentVector.get(loopCount) == component) rowIndex = loopCount;
				loopCount++;
			}

			// loop over run numbers:
			if(rowIndex > -1)
			{
				for(int k = runStart; k <= runEnd; k++)
				{
					provider.setDefaultRun(k);
					double value = provider.getData(tableName).getColumnValuesDouble(columnIndex).get(rowIndex);
					answer.add(k, value);
				}
			}
		}	
		provider.close();
		return answer;
	}
	
	// for tables like /calibration/dc/signal_generation/intrinsic_inefficiency or /calibration/forward_tagger/calorimeter/time
	// that have a single index rather than sector/layer/component
	public GraphErrors getConstantRunDependenceByIndex(String tableName, String variationName, int runStart, int runEnd, Date date, int columnIndex, int index) {
		GraphErrors answer = new GraphErrors();
		JDBCProvider provider = CcdbPackage.createProvider(this.address);
		provider.connect();
		if(provider.getIsConnected() != true) System.out.println("getConstantRunDependenceByIndex error. Not connected.");
		else
		{
			// find the row and column index of the requested constant:
			provider.setDefaultVariation(variationName);
			provider.setDefaultRun(runStart);
			provider.setDefaultDate(date);
			Assignment ass = provider.getData(tableName);
			int Nrows = ass.getRowCount();	
			Vector<Integer> indexVector = ass.getColumnValuesInt(0); // assuming first column gives the index of the entry
			int rowIndex = -99;
			int loopCount = 0;
			while(rowIndex < -1 && loopCount < Nrows) {
				if(indexVector.get(loopCount) == index) rowIndex = loopCount;
				loopCount++;
			}

			// loop over run numbers:
			if(rowIndex > -1)
			{
				for(int k = runStart; k <= runEnd; k++)
				{
					provider.setDefaultRun(k);
					double value = provider.getData(tableName).getColumnValuesDouble(columnIndex).get(rowIndex);
					answer.add(k, value);
				}
			}
		}	
		provider.close();
		return answer;	
	}
	
 	// ---------------- Main function ---------------------------- //
	// ----------------------------------------------------------- //

	public static void main(String[] args) {

	}   

}