package ccdbTools;

import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;
import org.jlab.groot.ui.TCanvas;

import java.util.Date;

public class FTAnalysis {

	public static void main(String[] args) {
		GStyle.getAxisAttributesX().setTitleFontSize(18);
		GStyle.getAxisAttributesX().setLabelFontSize(18);
		GStyle.getAxisAttributesY().setTitleFontSize(18);
		GStyle.getAxisAttributesY().setLabelFontSize(18);

		ConstantsComparer cc = new ConstantsComparer();

		TCanvas can = new TCanvas("can", 900, 850);
		can.divide(2, 2);
		can.getCanvas().setTitleSize(18);
		can.getCanvas().getPad(0).getAxisY().setRange(-15, 15);
		
		/// FTCAL time offset ///
		CcdbData data_TO_ideal = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 11, new Date(), "/calibration/ft/ftcal/time_offsets");
		CcdbData data_TO_smeared = new CcdbData("sqlite:///../../../clas12.smeared_constants_final.sqlite", "default", 17, new Date(), "/calibration/ft/ftcal/time_offsets");
		CcdbData data_TO_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(), "/calibration/ft/ftcal/time_offsets");

		GraphErrors g_TO_ideal = cc.getColumnGraphErrors(data_TO_ideal, 3, -1, 0, 0);
		GraphErrors g_TO_smeared = cc.getColumnGraphErrors(data_TO_smeared, 3, -1, 0, 0);
		GraphErrors g_TO_extracted = cc.getColumnGraphErrors(data_TO_extracted, 3, -1, 0, 0);
		
		g_TO_ideal.setMarkerColor(1);
		g_TO_ideal.setTitle("FTCAL TO");
		g_TO_ideal.setTitleX("component");
		g_TO_ideal.setTitleY("ns");
		g_TO_smeared.setMarkerColor(2);
		g_TO_smeared.setTitle("FTCAL TO");
		g_TO_smeared.setTitleX("component");
		g_TO_smeared.setTitleY("ns");
		g_TO_extracted.setMarkerColor(3);
		g_TO_extracted.setMarkerSize(4);
		g_TO_extracted.setTitle("FTCAL TO");
		g_TO_extracted.setTitleX("component");
		g_TO_extracted.setTitleY("ns");
		
		F1D f_TO_ideal = new F1D("f_TO_ideal", "[b] + [m]*x", 0, 333);
		f_TO_ideal.setParameter(0, 0.0);
		f_TO_ideal.setParameter(1, 0.0);
		f_TO_ideal.setLineColor(1);
		f_TO_ideal.setLineWidth(2);

		H1F h_TO_compare = cc.compareDiffH1D(data_TO_smeared, data_TO_extracted, 3, 0, 0, 25, -0.2, 0.2);
		h_TO_compare.setTitle("FTCAL TO");
		h_TO_compare.setTitleX("difference (ns)");
		
		F1D ff_TO = new F1D("ff_TO", "[amp]*gaus(x, [mean], [sigma])", -0.2, 0.2);
		ff_TO.setParameter(0, h_TO_compare.getBinContent(h_TO_compare.getMaximumBin()));
		ff_TO.setParameter(1, h_TO_compare.getMean());
		ff_TO.setParameter(2, h_TO_compare.getRMS());
		ff_TO.setLineColor(2);
		ff_TO.setLineWidth(2);
		
		DataFitter.fit(ff_TO, h_TO_compare, "RNQ");
		
		h_TO_compare.setLineWidth(2);
		h_TO_compare.setOptStat("1101001110");
		h_TO_compare.getStatBox().setFontSize(18);
		
		can.cd(0);
		//can.draw(g_TO_ideal);
		//can.draw(g_TO_smeared, "same");
		can.draw(g_TO_smeared);
		can.draw(g_TO_extracted, "same");
		can.draw(f_TO_ideal, "same");
		
		can.cd(1);
		can.draw(h_TO_compare);	


		/// FTHODO c2e ///
		CcdbData data_C2E_ideal = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 11, new Date(), "/calibration/ft/fthodo/charge_to_energy");
		CcdbData data_C2E_smeared = new CcdbData("sqlite:///../../../clas12.smeared_constants_final.sqlite", "default", 17, new Date(), "/calibration/ft/fthodo/charge_to_energy");
		CcdbData data_C2E_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(), "/calibration/ft/fthodo/charge_to_energy");

		GraphErrors g_C2E_ideal = cc.getColumnGraphErrors(data_C2E_ideal, 3, -1, 0, 0);
		GraphErrors g_C2E_smeared = cc.getColumnGraphErrors(data_C2E_smeared, 3, -1, 0, 0);
		GraphErrors g_C2E_extracted = cc.getColumnGraphErrors(data_C2E_extracted, 3, -1, 0, 0);
		
		g_C2E_ideal.setMarkerColor(1);
		g_C2E_ideal.setMarkerSize(2);
		g_C2E_ideal.setTitle("FTHODO C2E");
		g_C2E_ideal.setTitleX("component");
		g_C2E_ideal.setTitleY("mips charge");
		g_C2E_smeared.setMarkerColor(2);
		g_C2E_smeared.setTitle("FTHODO C2E");
		g_C2E_smeared.setTitleX("component");
		g_C2E_smeared.setTitleY("mips charge");
		g_C2E_extracted.setMarkerColor(3);
		g_C2E_extracted.setMarkerSize(4);
		g_C2E_extracted.setTitle("FTHODO C2E");
		g_C2E_extracted.setTitleX("component");
		g_C2E_extracted.setTitleY("mips charge");
		
		H1F h_C2E_compare = cc.compareDiffPercentH1D(data_C2E_smeared, data_C2E_extracted, 3, 0, 0, 25, -10.0, 10.0);
		h_C2E_compare.setTitle("FTHODO C2E");
		h_C2E_compare.setTitleX("percent difference (%)");
		
		F1D ff_C2E = new F1D("ff_C2E", "[amp]*gaus(x, [mean], [sigma])", -10.0, 10.0);
		ff_C2E.setParameter(0, h_C2E_compare.getBinContent(h_C2E_compare.getMaximumBin()));
		ff_C2E.setParameter(1, h_C2E_compare.getMean());
		ff_C2E.setParameter(2, h_C2E_compare.getRMS());
		ff_C2E.setLineColor(2);
		ff_C2E.setLineWidth(2);
		
		DataFitter.fit(ff_C2E, h_C2E_compare, "RNQ");
		
		h_C2E_compare.setLineWidth(2);
		h_C2E_compare.setOptStat("1101001110");
		h_C2E_compare.getStatBox().setFontSize(18);
		
		can.cd(2);
		can.draw(g_C2E_smeared);
		can.draw(g_C2E_extracted, "same");
		can.draw(g_C2E_ideal, "same");
		
		can.cd(3);
		can.draw(h_C2E_compare);	
	
	}

}
