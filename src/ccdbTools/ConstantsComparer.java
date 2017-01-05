package ccdbTools;

import java.util.Date;
import java.util.Vector;
import org.jlab.ccdb.Assignment;
import org.jlab.ccdb.CcdbPackage;
import org.jlab.ccdb.JDBCProvider;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;
import org.jlab.groot.ui.TCanvas;

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
					answer.addPoint(k, value, 0.0, 0.0);
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
					answer.addPoint(k, value, 0.0, 0.0);
				}
			}
		}	
		provider.close();
		return answer;	
	}
	
	public void compare2DataPrint(CcdbData data1, CcdbData data2, int columnIndex){
		if(data1.getNcolumns() != data2.getNcolumns() || data1.getNrows() != data2.getNrows())
		{
			System.out.println("Tables cannot be compared, different sizes");
		}
		else if(columnIndex >= data1.getNcolumns() || columnIndex < 0)
		{
			System.out.println("Invalid index");
		}
		else
		{
			Vector<Vector<String>> d1V = data1.getDataVector();
			Vector<Vector<String>> d2V = data2.getDataVector();
			for(int k = 0; k < data1.getNrows(); k++)
			{
				System.out.println(d1V.get(columnIndex).get(k) + "  " + d2V.get(columnIndex).get(k));
			}
		}
	}
	
	public void compare3DataPrint(CcdbData data1, CcdbData data2, CcdbData data3, int columnIndex){
		int rows = data1.getNrows();
		int columns = data1.getNcolumns();

		if(data2.getNrows() != rows || data3.getNrows() != rows || data2.getNcolumns() != columns || data3.getNcolumns() != columns)
		{
			System.out.println("Tables cannot be compared, different sizes");
		}
		else if(columnIndex >= columns || columnIndex < 0)
		{
			System.out.println("Invalid index");
		}
		else
		{
			Vector<Vector<String>> d1V = data1.getDataVector();
			Vector<Vector<String>> d2V = data2.getDataVector();
			Vector<Vector<String>> d3V = data3.getDataVector();
			for(int k = 0; k < data1.getNrows(); k++)
			{
				System.out.println(d1V.get(columnIndex).get(k) + "  " + d2V.get(columnIndex).get(k) + "  " + d3V.get(columnIndex).get(k));
			}
		}
	}
	
	// can set errIndex to -1 for no errors, can set sector to 0 for all sectors, can set layer to 0 for all layers
	public GraphErrors getColumnGraphErrors(CcdbData data, int columnIndex, int errIndex, int sector, int layer){
		GraphErrors answer = new GraphErrors();

		if(columnIndex < 0 || columnIndex >= data.getNcolumns())
		{
			System.out.println("Invalid index");
		}
		else
		{
			Vector<Vector<String>> dV = data.getDataVector();
			int counter = 0;
			for(int k = 0; k < data.getNrows(); k++)
			{
				if((Integer.parseInt(dV.get(0).get(k)) == sector || sector <= 0) && (Integer.parseInt(dV.get(1).get(k)) == layer || layer <= 0)) // assuming first column is sector, second column is layer
				{
					counter++;
					if(errIndex >= 0 && errIndex < data.getNcolumns()) answer.addPoint(counter, Double.parseDouble(dV.get(columnIndex).get(k)), 0.0, Double.parseDouble(dV.get(errIndex).get(k)));
					else answer.addPoint(counter, Double.parseDouble(dV.get(columnIndex).get(k)), 0.0, 0.0);
				}
			}
		}
		
		return answer;
	}
	
	// can set sector to 0 for all sectors
	public H1F compareDiffH1D(CcdbData data1, CcdbData data2, int columnIndex, int sector, int layer, int Nbins, double min, double max){
		H1F answer = new H1F("h", Nbins, min, max);
		
		if(data1.getNrows() != data2.getNrows())
		{
			System.out.println("Tables cannot be compared, different sizes");
		}
		else if(columnIndex >= data1.getNcolumns() || columnIndex >= data2.getNcolumns() || columnIndex < 0)
		{
			System.out.println("Invalid index");
		}
		else
		{
			Vector<Vector<String>> dV1 = data1.getDataVector();
			Vector<Vector<String>> dV2 = data2.getDataVector();
			for(int k = 0; k < data1.getNrows(); k++)
			{
				if((Integer.parseInt(dV1.get(0).get(k)) == sector || sector <= 0) && (Integer.parseInt(dV1.get(1).get(k)) == layer || layer <= 0)) // assuming first column is sector, second column is layer
				{
					answer.fill(Double.parseDouble(dV1.get(columnIndex).get(k)) - Double.parseDouble(dV2.get(columnIndex).get(k)));
				}
			}
		}
		
		return answer;
	}
	
	// can set sector to 0 for all sectors
	public H1F compareDiffPercentH1D(CcdbData data1, CcdbData data2, int columnIndex, int sector, int layer, int Nbins, double min, double max){
		H1F answer = new H1F("h", Nbins, min, max);
		
		if(data1.getNrows() != data2.getNrows())
		{
			System.out.println("Tables cannot be compared, different sizes");
		}
		else if(columnIndex >= data1.getNcolumns() || columnIndex >= data2.getNcolumns() || columnIndex < 0)
		{
			System.out.println("Invalid index");
		}
		else
		{
			Vector<Vector<String>> dV1 = data1.getDataVector();
			Vector<Vector<String>> dV2 = data2.getDataVector();
			for(int k = 0; k < data1.getNrows(); k++)
			{
				if((Integer.parseInt(dV1.get(0).get(k)) == sector || sector <= 0) && (Integer.parseInt(dV1.get(1).get(k)) == layer || layer <= 0)) // assuming first column is sector, second column is layer
				{
					double val1 = Double.parseDouble(dV1.get(columnIndex).get(k));
					double val2 = Double.parseDouble(dV2.get(columnIndex).get(k));
					answer.fill(100.0*((val1 - val2)/val1));
				}
			}
		}
		
		return answer;
	}
	
 	// ---------------- Main function ---------------------------- //
	// ----------------------------------------------------------- //

	public static void main(String[] args) {
		GStyle.getAxisAttributesX().setTitleFontSize(20);
		GStyle.getAxisAttributesX().setLabelFontSize(20);
		GStyle.getAxisAttributesY().setTitleFontSize(20);
		GStyle.getAxisAttributesY().setLabelFontSize(20);

		ConstantsComparer cc = new ConstantsComparer();
		
		Vector<String> tableNames = new Vector<>();
		Vector<String> quantNames = new Vector<>();
		Vector<Integer> columnIndicies = new Vector<>();
		Vector<Integer> errIndicies = new Vector<>();
		Vector<Integer> Nsectors = new Vector<>();
		Vector<Integer> Nlayers = new Vector<>();
		Vector<Double> DeltaHistMin = new Vector<>();
		Vector<Double> DeltaHistMax = new Vector<>();
		Vector<String> units = new Vector<>();

//		// quantity 0: ftof atten. column 3 (attlen_left)
//		tableNames.add("/calibration/ftof/attenuation");
//		quantNames.add("ftof attlen_left");
//		columnIndicies.add(3);
//		//errIndicies.add(5);
//		errIndicies.add(-1);
//		//Nsectors.add(6);
//		Nsectors.add(-99);
//		Nlayers.add(3);
//		DeltaHistMin.add(-10.0);
//		DeltaHistMax.add(10.0);
//		units.add("cm");
//
//		// quantity 1: ftof veff column 3 (veff_left)
//		tableNames.add("/calibration/ftof/effective_velocity");
//		quantNames.add("ftof veff_left");
//		columnIndicies.add(3);
//		//errIndicies.add(5);
//		errIndicies.add(-1);
//		//Nsectors.add(6);
//		Nsectors.add(-99);
//		Nlayers.add(3);
//		DeltaHistMin.add(-10.0);
//		DeltaHistMax.add(10.0);
//		units.add("cm/ns");
//
//		tableNames.add("/calibration/ftof/timing_offset");
//		quantNames.add("ftof t L_R");
//		columnIndicies.add(3);
//		errIndicies.add(-1);
//		//Nsectors.add(6);
//		Nsectors.add(-99);
//		Nlayers.add(3);
//		DeltaHistMin.add(-10.0);
//		DeltaHistMax.add(10.0);
//		units.add("ns");
//
//		tableNames.add("/calibration/ftof/timing_offset");
//		quantNames.add("ftof t p2p");
//		columnIndicies.add(4);
//		errIndicies.add(-1);
//		//Nsectors.add(6);
//		Nsectors.add(-99);
//		Nlayers.add(3);
//		DeltaHistMin.add(-10.0);
//		DeltaHistMax.add(10.0);
//		units.add("ns");
//
//		tableNames.add("/calibration/ctof/attenuation");
//		quantNames.add("ctof attlen_up");
//		columnIndicies.add(3);
//		//errIndicies.add(5);
//		errIndicies.add(-1);
//		Nsectors.add(1);
//		Nlayers.add(1);
//		DeltaHistMin.add(-150.0);
//		DeltaHistMax.add(150.0);
//		units.add("cm");
//		
//		tableNames.add("/calibration/ctof/attenuation");
//		quantNames.add("ctof attlen_down");
//		columnIndicies.add(4);
//		errIndicies.add(6);
//		Nsectors.add(1);
//		Nlayers.add(1);
//		DeltaHistMin.add(-150.0);
//		DeltaHistMax.add(150.0);
//		units.add("cm");
//
//		tableNames.add("/calibration/ctof/effective_velocity");
//		quantNames.add("ctof veff_up");
//		columnIndicies.add(3);
//		errIndicies.add(5);
//		Nsectors.add(1);
//		Nlayers.add(1);
//		DeltaHistMin.add(-10.0);
//		DeltaHistMax.add(10.0);
//		units.add("cm/ns");
//
//		tableNames.add("/calibration/ctof/effective_velocity");
//		quantNames.add("ctof veff_down");
//		columnIndicies.add(4);
//		errIndicies.add(6);
//		Nsectors.add(1);
//		Nlayers.add(1);
//		DeltaHistMin.add(-10.0);
//		DeltaHistMax.add(10.0);
//		units.add("cm/ns");
//
//		tableNames.add("/calibration/ctof/timing_offset");
//		quantNames.add("ctof t up_down");
//		columnIndicies.add(3);
//		errIndicies.add(-1);
//		Nsectors.add(1);
//		Nlayers.add(1);
//		DeltaHistMin.add(-10.0);
//		DeltaHistMax.add(10.0);
//		units.add("ns");
//
//		tableNames.add("/calibration/ctof/timing_offset");
//		quantNames.add("ctof t p2p");
//		columnIndicies.add(4);
//		errIndicies.add(-1);
//		Nsectors.add(1);
//		Nlayers.add(1);
//		DeltaHistMin.add(-10.0);
//		DeltaHistMax.add(10.0);
//		units.add("ns");
//
//		tableNames.add("/calibration/ft/ftcal/time_offsets");
//		quantNames.add("ftcal TO");
//		columnIndicies.add(3);
//		errIndicies.add(4);
//		Nsectors.add(1);
//		Nlayers.add(1);
//		DeltaHistMin.add(-10.0);
//		DeltaHistMax.add(10.0);
//		units.add("ns");
//
//		tableNames.add("/calibration/ft/fthodo/charge_to_energy");
//		quantNames.add("fthodo mips_char");
//		columnIndicies.add(3);
//		errIndicies.add(-1);
//		Nsectors.add(8);
//		Nlayers.add(2);
//		DeltaHistMin.add(-1001.0);
//		DeltaHistMax.add(1001.0);
//		units.add("mips charge");
//
//		tableNames.add("/calibration/cnd/time_offsets_LR");
//		quantNames.add("cnd time_offset_LR");
//		columnIndicies.add(3);
//		errIndicies.add(4);
//		Nsectors.add(-99);
//		Nlayers.add(3);
//		DeltaHistMin.add(-50.0);
//		DeltaHistMax.add(50.0);
//		units.add("ns");
//
//		tableNames.add("/calibration/cnd/time_offsets_layer");
//		quantNames.add("cnd time_offset_sector");
//		columnIndicies.add(3);
//		errIndicies.add(4);
//		Nsectors.add(-99);
//		Nlayers.add(3);
//		DeltaHistMin.add(-10.0);
//		DeltaHistMax.add(10.0);
//		units.add("ns");
//
//		tableNames.add("/calibration/cnd/veff");
//		quantNames.add("cnd veff");
//		columnIndicies.add(3);
//		errIndicies.add(4);
//		Nsectors.add(-99);
//		Nlayers.add(3);
//		DeltaHistMin.add(-50.0);
//		DeltaHistMax.add(10.0);
//		units.add("ns");

		Vector<Vector<Vector<GraphErrors>>> g_ideal = new Vector<>();
		Vector<Vector<Vector<GraphErrors>>> g_smeared = new Vector<>();
		Vector<Vector<Vector<GraphErrors>>> g_extracted = new Vector<>();

		Vector<Vector<Vector<H1F>>> h_compare = new Vector<>();
		Vector<Vector<Vector<F1D>>> ff = new Vector<>();
		
		Vector<Vector<TCanvas>> cans = new Vector<>();
		
		int Nquant = tableNames.size();
		
		for(int iquant = 0; iquant < Nquant; iquant++)
		{
			CcdbData data_ideal = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 11, new Date(), tableNames.get(iquant));
			CcdbData data_smeared = new CcdbData("sqlite:///../../../clas12.smeared_constants_final.sqlite", "default", 17, new Date(), tableNames.get(iquant));
			CcdbData data_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(), tableNames.get(iquant));

			// 1481690200000 is milliseconds from midnight Jan 1 1970 to Dec 13 2016 at about 11:45 pm

			//CcdbData data_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(1481690200000L), tableNames.get(iquant));
			//CcdbData data_ideal = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(1481690200000L), tableNames.get(iquant));
			//CcdbData data_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(1482422683994L), tableNames.get(iquant));
			//CcdbData data_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(1482422836459L), tableNames.get(iquant));
			//CcdbData data_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(1482422914631L), tableNames.get(iquant));
			//CcdbData data_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(1482422980063L), tableNames.get(iquant));

			// Thursday, Dec. 22, 2016, 10:45am - about to add CND constants to CCDB w/ run# 17 (it. 5), current time is 1482421491186
			// 11:03am just added time_offsets_LR, time_offsets_layer, and veff ("straight line original"), time is 1482422683994
			// 11:06am just added veff ("straight line with sys. corr."), time is 1482422836459
			// 11:08am just added veff ("top hat original"), time is 1482422914631
			// 11:09am just added veff ("top hat with sys. corr."), time is 1482422980063
			
			g_ideal.add(new Vector<>());
			g_smeared.add(new Vector<>());
			g_extracted.add(new Vector<>());

			h_compare.add(new Vector<>());
			ff.add(new Vector<>());
			
			cans.add(new Vector<>());

			for(int ilay = 0; ilay < Nlayers.get(iquant); ilay++)
			{
				int Nsec = Nsectors.get(iquant);
				if(Nsec < 1) Nsec = 1; // for the case when summing over sectors

				g_ideal.get(iquant).add(new Vector<>());
				g_smeared.get(iquant).add(new Vector<>());
				g_extracted.get(iquant).add(new Vector<>());

				h_compare.get(iquant).add(new Vector<>());
				ff.get(iquant).add(new Vector<>());
				
				cans.get(iquant).add(new TCanvas(String.format("can_%d_%d", iquant, ilay), 1350, 850));
				cans.get(iquant).get(ilay).divide(Nsec, 2);
				cans.get(iquant).get(ilay).getCanvas().setTitleSize(20);

				for(int isec = 0; isec < Nsec; isec++)
				{
					int temp_isec = isec;
					if(Nsectors.get(iquant) < 1) temp_isec = -1;

					g_ideal.get(iquant).get(ilay).add(cc.getColumnGraphErrors(data_ideal, columnIndicies.get(iquant), errIndicies.get(iquant), temp_isec+1, ilay+1));
					g_smeared.get(iquant).get(ilay).add(cc.getColumnGraphErrors(data_smeared, columnIndicies.get(iquant), errIndicies.get(iquant), temp_isec+1, ilay+1));
					g_extracted.get(iquant).get(ilay).add(cc.getColumnGraphErrors(data_extracted, columnIndicies.get(iquant), errIndicies.get(iquant), temp_isec+1, ilay+1));

					g_ideal.get(iquant).get(ilay).get(isec).setMarkerColor(1);
					g_ideal.get(iquant).get(ilay).get(isec).setTitle(String.format("%s sec%d lay%d", quantNames.get(iquant), temp_isec+1, ilay+1));
					g_ideal.get(iquant).get(ilay).get(isec).setTitleX("compoent");
					g_ideal.get(iquant).get(ilay).get(isec).setTitleY(units.get(iquant));
					g_smeared.get(iquant).get(ilay).get(isec).setMarkerColor(2);
					g_extracted.get(iquant).get(ilay).get(isec).setMarkerColor(3);

					//h_compare.get(iquant).get(ilay).add(cc.compareDiffH1D(data_smeared, data_extracted, columnIndicies.get(iquant), temp_isec+1, ilay+1, 25, DeltaHistMin.get(iquant), DeltaHistMax.get(iquant)));
					//h_compare.get(iquant).get(ilay).get(isec).setTitle("distorted - extracted");
					//h_compare.get(iquant).get(ilay).get(isec).setTitleX(units.get(iquant));
					h_compare.get(iquant).get(ilay).add(cc.compareDiffPercentH1D(data_smeared, data_extracted, columnIndicies.get(iquant), temp_isec+1, ilay+1, 25, DeltaHistMin.get(iquant), DeltaHistMax.get(iquant)));
					h_compare.get(iquant).get(ilay).get(isec).setTitle("100*(distorted - extracted)/distorted");
					h_compare.get(iquant).get(ilay).get(isec).setTitleX("percent difference (%)");
					
					ff.get(iquant).get(ilay).add(new F1D(String.format("ff_%d_%d", iquant, ilay), "[amp]*gaus(x, [mean], [sigma])", DeltaHistMin.get(iquant), DeltaHistMax.get(iquant)));
					ff.get(iquant).get(ilay).get(isec).setParameter(0, h_compare.get(iquant).get(ilay).get(isec).getBinContent(h_compare.get(iquant).get(ilay).get(isec).getMaximumBin()));
					ff.get(iquant).get(ilay).get(isec).setParameter(1, h_compare.get(iquant).get(ilay).get(isec).getMean());
					ff.get(iquant).get(ilay).get(isec).setParameter(2, h_compare.get(iquant).get(ilay).get(isec).getRMS());
					ff.get(iquant).get(ilay).get(isec).setLineColor(2);
					ff.get(iquant).get(ilay).get(isec).setLineWidth(2);

					DataFitter.fit(ff.get(iquant).get(ilay).get(isec), h_compare.get(iquant).get(ilay).get(isec), "RNQ");
					
					h_compare.get(iquant).get(ilay).get(isec).setLineWidth(2);
					h_compare.get(iquant).get(ilay).get(isec).setOptStat("1111111110");
					h_compare.get(iquant).get(ilay).get(isec).getStatBox().setFontSize(20);
				
					cans.get(iquant).get(ilay).cd(isec);
					cans.get(iquant).get(ilay).draw(g_ideal.get(iquant).get(ilay).get(isec));
					cans.get(iquant).get(ilay).draw(g_smeared.get(iquant).get(ilay).get(isec), "same");
					cans.get(iquant).get(ilay).draw(g_extracted.get(iquant).get(ilay).get(isec), "same");

					cans.get(iquant).get(ilay).cd(isec + Nsec);
					cans.get(iquant).get(ilay).draw(h_compare.get(iquant).get(ilay).get(isec));
				}
			}
		}
	}   

}