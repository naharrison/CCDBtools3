package ccdbTools;

import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;
import org.jlab.groot.ui.TCanvas;

import java.util.Date;

public class CNDAnalysis {

	public static void main(String[] args) {
		GStyle.getAxisAttributesX().setTitleFontSize(18);
		GStyle.getAxisAttributesX().setLabelFontSize(18);
		GStyle.getAxisAttributesY().setTitleFontSize(18);
		GStyle.getAxisAttributesY().setLabelFontSize(18);

		ConstantsComparer cc = new ConstantsComparer();

		TCanvas can = new TCanvas("can", 900, 850);
		can.divide(2, 3);
		can.getCanvas().setTitleSize(18);
		can.getCanvas().getPad(0).getAxisY().setRange(-15, 15);
		can.getCanvas().getPad(2).getAxisY().setRange(-20, 20);
		can.getCanvas().getPad(4).getAxisY().setRange(5, 28);
		
		/// left_right timing offset ///
		CcdbData data_LR_ideal = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 11, new Date(), "/calibration/cnd/time_offsets_LR");
		CcdbData data_LR_smeared = new CcdbData("sqlite:///../../../clas12.smeared_constants_final.sqlite", "default", 17, new Date(), "/calibration/cnd/time_offsets_LR");
		CcdbData data_LR_extracted = new CcdbData(-1, "mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(), "/calibration/cnd/time_offsets_LR");
		// using negative values: ----------------^^

		GraphErrors g_LR_ideal = cc.getColumnGraphErrors(data_LR_ideal, 3, -1, 0, 0);
		GraphErrors g_LR_smeared = cc.getColumnGraphErrors(data_LR_smeared, 3, -1, 0, 0);
		GraphErrors g_LR_extracted = cc.getColumnGraphErrors(data_LR_extracted, 3, -1, 0, 0);
		
		g_LR_ideal.setMarkerColor(1);
		g_LR_ideal.setTitle("CND LR");
		g_LR_ideal.setTitleX("component");
		g_LR_ideal.setTitleY("ns");
		g_LR_smeared.setMarkerColor(2);
		g_LR_smeared.setTitle("CND LR");
		g_LR_smeared.setTitleX("component");
		g_LR_smeared.setTitleY("ns");
		g_LR_extracted.setMarkerColor(3);
		g_LR_extracted.setMarkerSize(4);
		g_LR_extracted.setTitle("CND LR");
		g_LR_extracted.setTitleX("component");
		g_LR_extracted.setTitleY("ns");
		
		F1D f_LR_ideal = new F1D("f_LR_ideal", "[b] + [m]*x", 0, 73);
		f_LR_ideal.setParameter(0, 0.0);
		f_LR_ideal.setParameter(1, 0.0);
		f_LR_ideal.setLineColor(1);
		f_LR_ideal.setLineWidth(2);

		H1F h_LR_compare = cc.compareDiffH1D(data_LR_smeared, data_LR_extracted, 3, 0, 0, 25, -10.0, 10.0);
		h_LR_compare.setTitle("CND LR");
		h_LR_compare.setTitleX("difference (ns)");
		
		F1D ff_LR = new F1D("ff_LR", "[amp]*gaus(x, [mean], [sigma])", -10.0, 10.0);
		ff_LR.setParameter(0, h_LR_compare.getBinContent(h_LR_compare.getMaximumBin()));
		ff_LR.setParameter(1, h_LR_compare.getMean());
		ff_LR.setParameter(2, h_LR_compare.getRMS());
		ff_LR.setLineColor(2);
		ff_LR.setLineWidth(2);
		
		DataFitter.fit(ff_LR, h_LR_compare, "RNQ");
		
		h_LR_compare.setLineWidth(2);
		h_LR_compare.setOptStat("1101001110");
		h_LR_compare.getStatBox().setFontSize(18);
		
		can.cd(0);
		//can.draw(g_LR_ideal);
		//can.draw(g_LR_smeared, "same");
		can.draw(g_LR_smeared);
		can.draw(g_LR_extracted, "same");
		can.draw(f_LR_ideal, "same");
		
		can.cd(1);
		can.draw(h_LR_compare);	
	
		/// time offsets layer ///
		/// have to fill graphs manually for this one
		CcdbData data_layer_smeared = new CcdbData("sqlite:///../../../clas12.smeared_constants_final.sqlite", "default", 17, new Date(), "/calibration/cnd/time_offsets_layer");
		CcdbData data_layer_extracted = new CcdbData(-1, "mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(), "/calibration/cnd/time_offsets_layer");
		// using negative values: -------------------^^

		GraphErrors g_layer_smeared = new GraphErrors();
		GraphErrors g_layer_extracted = new GraphErrors();
		
		H1F h_layer_compare = new H1F("h_layer_compare", 25, -6.0, 6.0);

		F1D ff_layer = new F1D("ff_layer", "[amp]*gaus(x, [mean], [sigma])", -6.0, 6.0);
		
		int counter = 1;
		for(int isec = 1; isec <= 24; isec++) // 24 sectors (3 layers per sector, no components)
		{
			double L1_smeared = data_layer_smeared.getConstantBySLCDouble(3, isec, 1, 0);
			double L2_smeared = data_layer_smeared.getConstantBySLCDouble(3, isec, 2, 0);
			double L3_smeared = data_layer_smeared.getConstantBySLCDouble(3, isec, 3, 0);
			double L1_extracted = data_layer_extracted.getConstantBySLCDouble(3, isec, 1, 0);
			double L2_extracted = data_layer_extracted.getConstantBySLCDouble(3, isec, 2, 0);
			double L3_extracted = data_layer_extracted.getConstantBySLCDouble(3, isec, 3, 0);
			
			g_layer_smeared.addPoint(counter, L1_smeared - L3_smeared, 0.0, 0.0);
			g_layer_extracted.addPoint(counter, L1_extracted - L3_extracted, 0.0, 0.0);
			counter++;
			g_layer_smeared.addPoint(counter, L2_smeared - L3_smeared, 0.0, 0.0);
			g_layer_extracted.addPoint(counter, L2_extracted - L3_extracted, 0.0, 0.0);
			counter++;
			
			h_layer_compare.fill((L1_smeared - L3_smeared) - (L1_extracted - L3_extracted));
			h_layer_compare.fill((L2_smeared - L3_smeared) - (L2_extracted - L3_extracted));
		}
		
		g_layer_smeared.setMarkerColor(2);
		g_layer_smeared.setTitle("CND layer");
		g_layer_smeared.setTitleX("component");
		g_layer_smeared.setTitleY("ns");
		g_layer_extracted.setMarkerColor(3);
		g_layer_extracted.setMarkerSize(4);
		g_layer_extracted.setTitle("CND layer");
		g_layer_extracted.setTitleX("component");
		g_layer_extracted.setTitleY("ns");

		h_layer_compare.setTitle("CND layer");
		h_layer_compare.setTitleX("difference (ns)");
		
		F1D f_layer_ideal = new F1D("f_layer_ideal", "[b] + [m]*x", 0, 49);
		f_layer_ideal.setParameter(0, 0.0);
		f_layer_ideal.setParameter(1, 0.0);
		f_layer_ideal.setLineColor(1);
		f_layer_ideal.setLineWidth(2);

		ff_layer.setParameter(0, h_layer_compare.getBinContent(h_layer_compare.getMaximumBin()));
		ff_layer.setParameter(1, h_layer_compare.getMean());
		ff_layer.setParameter(2, h_layer_compare.getRMS());
		ff_layer.setLineColor(2);
		ff_layer.setLineWidth(2);
		
		DataFitter.fit(ff_layer, h_layer_compare, "RNQ");
		
		h_layer_compare.setLineWidth(2);
		h_layer_compare.setOptStat("1101001110");
		h_layer_compare.getStatBox().setFontSize(18);
		
		can.cd(2);
		can.draw(g_layer_smeared);
		can.draw(g_layer_extracted, "same");
		can.draw(f_layer_ideal, "same");
		
		can.cd(3);
		can.draw(h_layer_compare);	
	
		/// veff ///
		CcdbData data_veff_ideal = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 11, new Date(), "/calibration/cnd/veff");
		CcdbData data_veff_smeared = new CcdbData("sqlite:///../../../clas12.smeared_constants_final.sqlite", "default", 17, new Date(), "/calibration/cnd/veff");
		CcdbData data_veff_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(), "/calibration/cnd/veff");

		GraphErrors g_veff_ideal = cc.getColumnGraphErrors(data_veff_ideal, 3, -1, 0, 0);
		GraphErrors g_veff_smeared = cc.getColumnGraphErrors(data_veff_smeared, 3, -1, 0, 0);
		GraphErrors g_veff_extracted = cc.getColumnGraphErrors(data_veff_extracted, 3, -1, 0, 0);
		
		g_veff_ideal.setMarkerColor(1);
		g_veff_ideal.setTitle("CND veff");
		g_veff_ideal.setTitleX("component");
		g_veff_ideal.setTitleY("cm/ns");
		g_veff_smeared.setMarkerColor(2);
		g_veff_smeared.setTitle("CND veff");
		g_veff_smeared.setTitleX("component");
		g_veff_smeared.setTitleY("cm/ns");
		g_veff_extracted.setMarkerColor(3);
		g_veff_extracted.setMarkerSize(4);
		g_veff_extracted.setTitle("CND veff");
		g_veff_extracted.setTitleX("component");
		g_veff_extracted.setTitleY("cm/ns");
		
		F1D f_veff_ideal = new F1D("f_veff_ideal", "[b] + [m]*x", 0, 145);
		f_veff_ideal.setParameter(0, 16.0);
		f_veff_ideal.setParameter(1, 0.0);
		f_veff_ideal.setLineColor(1);
		f_veff_ideal.setLineWidth(2);

		H1F h_veff_compare = cc.compareDiffPercentH1D(data_veff_smeared, data_veff_extracted, 3, 0, 0, 25, -10.0, 10.0);
		h_veff_compare.setTitle("CND veff");
		h_veff_compare.setTitleX("percent difference (%)");
		
		F1D ff_veff = new F1D("ff_veff", "[amp]*gaus(x, [mean], [sigma])", -10.0, 10.0);
		ff_veff.setParameter(0, h_veff_compare.getBinContent(h_veff_compare.getMaximumBin()));
		ff_veff.setParameter(1, h_veff_compare.getMean());
		ff_veff.setParameter(2, h_veff_compare.getRMS());
		ff_veff.setLineColor(2);
		ff_veff.setLineWidth(2);
		
		DataFitter.fit(ff_veff, h_veff_compare, "RNQ");
		
		h_veff_compare.setLineWidth(2);
		h_veff_compare.setOptStat("1101001110");
		h_veff_compare.getStatBox().setFontSize(18);
		
		can.cd(4);
		//can.draw(g_veff_ideal);
		//can.draw(g_veff_smeared, "same");
		can.draw(g_veff_smeared);
		can.draw(g_veff_extracted, "same");
		can.draw(f_veff_ideal, "same");
		
		can.cd(5);
		can.draw(h_veff_compare);	
	
	}

}
