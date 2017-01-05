package ccdbTools;

import java.util.Date;
import java.util.Vector;
import org.jlab.ccdb.Assignment;
import org.jlab.ccdb.JDBCProvider;
import org.jlab.ccdb.CcdbPackage;

public class CcdbData {

	// ------------------- Fields -------------------------------- //
	// ----------------------------------------------------------- //

	private String address;
	private String variationName;
	private int runNumber;
	private Date date;
	private String tableName;
	private int Ncolumns;
	private int Nrows;
	private Vector<Vector<String>> data = new Vector<>();

	// ------------------ Constructors --------------------------- //
	// ----------------------------------------------------------- //

	public CcdbData(String TABLENAME) {
		this.tableName = TABLENAME;

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
		
		System.out.println("Variation is set to default value of default.");
		this.variationName = "default";
		System.out.println("Run number is set to default value of 11.");
		this.runNumber = 11;
		System.out.println("Date is set to default value of now.");
		this.date = new Date();
		
		this.retrieveData();
	}

	public CcdbData(String ADDRESS, String VARIATION, int RUNNUMBER, Date DATE, String TABLENAME) {
		this.address = ADDRESS;
		this.variationName = VARIATION;
		this.runNumber = RUNNUMBER;
		this.date = DATE;
		this.tableName = TABLENAME;

		this.retrieveData();
	}
	
	// quick hack to fix Gavin's opposite sign convention... use sparingly
	public CcdbData(int negate, String ADDRESS, String VARIATION, int RUNNUMBER, Date DATE, String TABLENAME) {
		this.address = ADDRESS;
		this.variationName = VARIATION;
		this.runNumber = RUNNUMBER;
		this.date = DATE;
		this.tableName = TABLENAME;

		this.retrieveDataNegate();
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
    
	private void retrieveData(){
		JDBCProvider provider = CcdbPackage.createProvider(this.address);
		provider.connect();
		if(provider.getIsConnected() != true) System.out.println("retrieveData error. Could not connected.");
		else
		{
			provider.setDefaultVariation(this.variationName);
			provider.setDefaultRun(this.runNumber);
			provider.setDefaultDate(this.date);
			
			Assignment ass = provider.getData(this.tableName);
			this.Ncolumns = ass.getColumnCount();
			this.Nrows = ass.getRowCount();
			
			for(int j = 0; j < Ncolumns; j++) this.data.add(ass.getColumnValuesString(j));
		}
		provider.close();
	}
	
	// hack... use sparingly
	private void retrieveDataNegate(){
		JDBCProvider provider = CcdbPackage.createProvider(this.address);
		provider.connect();
		if(provider.getIsConnected() != true) System.out.println("retrieveData error. Could not connected.");
		else
		{
			provider.setDefaultVariation(this.variationName);
			provider.setDefaultRun(this.runNumber);
			provider.setDefaultDate(this.date);
			
			Assignment ass = provider.getData(this.tableName);
			this.Ncolumns = ass.getColumnCount();
			this.Nrows = ass.getRowCount();
			
			for(int j = 0; j < Ncolumns; j++)
			{
				this.data.add(new Vector<>());
				for(int k = 0; k < Nrows; k++)
				{
					if(j <= 2) this.data.get(j).add(ass.getColumnValuesString(j).get(k)); // don't negate sector/layer/component
					else
					{
						double negElement = -1.0*Double.parseDouble(ass.getColumnValuesString(j).get(k));
						this.data.get(j).add(String.valueOf(negElement));
					}
				}
			}
		}
		provider.close();
	}

	public void show(){
		for(int k = 0; k < Nrows; k++)
		{
			for(int j = 0; j < Ncolumns; j++)
			{
				System.out.print(this.data.get(j).get(k) + "    ");
			}
			System.out.println("");
		}
	}

	public double getConstantBySLCDouble(int columnIndex, int sector, int layer, int component){
		// there are faster ways to do this, but this is the easiest and safest
		for(int k = 0; k < Nrows; k++)
		{
			int currentSec = Integer.parseInt(this.data.get(0).get(k));
			int currentLay = Integer.parseInt(this.data.get(1).get(k));
			int currentComp = Integer.parseInt(this.data.get(2).get(k));
			if(currentSec == sector && currentLay == layer && currentComp == component)
			{
				return Double.parseDouble(this.data.get(columnIndex).get(k));
			}
		}
		
		return -123.4;
	}
	
	public int getConstantBySLCInt(int columnIndex, int sector, int layer, int component){
		// there are faster ways to do this, but this is the easiest and safest
		for(int k = 0; k < Nrows; k++)
		{
			int currentSec = Integer.parseInt(this.data.get(0).get(k));
			int currentLay = Integer.parseInt(this.data.get(1).get(k));
			int currentComp = Integer.parseInt(this.data.get(2).get(k));
			if(currentSec == sector && currentLay == layer && currentComp == component)
			{
				return Integer.parseInt(this.data.get(columnIndex).get(k));
			}
		}
		
		return -1234;
	}

	public Vector<Vector<String>> getDataVector(){
		return this.data;
	}
	
	public int getNcolumns(){
		return this.Ncolumns;
	}

	public int getNrows(){
		return this.Nrows;
	}

  	// ---------------- Main function ---------------------------- //
	// ----------------------------------------------------------- //

	public static void main(String[] args) {
		CcdbData ctof_atten_data = new CcdbData("/calibration/ctof/attenuation");
		CcdbData ctof_status_data = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 11, new Date(), "/calibration/ctof/status");
		ctof_atten_data.show();
		System.out.println();
		System.out.println(ctof_atten_data.getConstantBySLCDouble(3, 1, 1, 4)); // column3, SLC=1,1,4
		System.out.println(ctof_status_data.getConstantBySLCDouble(3, 1, 1, 4)); // column3, SLC=1,1,4
		System.out.println(ctof_status_data.getConstantBySLCInt(3, 1, 1, 4)); // column3, SLC=1,1,4
	}   
}
