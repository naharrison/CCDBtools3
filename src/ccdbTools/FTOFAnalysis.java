package ccdbTools;

import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;
import org.jlab.groot.ui.TCanvas;

import java.util.Date;

public class FTOFAnalysis {

	public static void main(String[] args) {
		GStyle.getAxisAttributesX().setTitleFontSize(18);
		GStyle.getAxisAttributesX().setLabelFontSize(18);
		GStyle.getAxisAttributesY().setTitleFontSize(18);
		GStyle.getAxisAttributesY().setLabelFontSize(18);

		ConstantsComparer cc = new ConstantsComparer();

		TCanvas can = new TCanvas("can", 900, 850);
		can.divide(2, 3);
		can.getCanvas().setTitleSize(18);
		can.getCanvas().getPad(2).getAxisY().setRange(5, 28);
		can.getCanvas().getPad(4).getAxisY().setRange(-15, 15);
		
		/// attenuation length ///
		CcdbData data_atten_ideal = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 11, new Date(), "/calibration/ftof/attenuation");
		CcdbData data_atten_smeared = new CcdbData("sqlite:///../../../clas12.smeared_constants_final.sqlite", "default", 17, new Date(), "/calibration/ftof/attenuation");
		CcdbData data_atten_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(), "/calibration/ftof/attenuation");

		GraphErrors g_atten_ideal = cc.getColumnGraphErrors(data_atten_ideal, 3, -1, 1, 2);
		GraphErrors g_atten_smeared = cc.getColumnGraphErrors(data_atten_smeared, 3, -1, 1, 2);
		GraphErrors g_atten_extracted = cc.getColumnGraphErrors(data_atten_extracted, 3, -1, 1, 2);
		
		g_atten_ideal.setMarkerColor(1);
		g_atten_ideal.setTitle("FTOF 1B atten. length (sec 1)");
		g_atten_ideal.setTitleX("paddle");
		g_atten_ideal.setTitleY("cm");
		g_atten_smeared.setMarkerColor(2);
		g_atten_smeared.setTitle("FTOF 1B atten. length (sec 1)");
		g_atten_smeared.setTitleX("paddle");
		g_atten_smeared.setTitleY("cm");
		g_atten_extracted.setMarkerColor(3);
		g_atten_extracted.setMarkerSize(4);
		g_atten_extracted.setTitle("FTOF 1B atten. length (sec 1)");
		g_atten_extracted.setTitleX("paddle");
		g_atten_extracted.setTitleY("cm");
		
		F1D f_atten_ideal = new F1D("f_atten_ideal", "[b] + [m]*x", 0, 63);
		f_atten_ideal.setParameter(0, 135.0);
		f_atten_ideal.setParameter(1, 4.079);
		f_atten_ideal.setLineColor(1);
		f_atten_ideal.setLineWidth(2);

		H1F h_atten_compare = cc.compareDiffPercentH1D(data_atten_smeared, data_atten_extracted, 3, 0, 0, 25, -8.0, 8.0);
		h_atten_compare.setTitle("FTOF 1B atten. length (sec 1)");
		h_atten_compare.setTitleX("percent difference (%)");
		
		F1D ff_atten = new F1D("ff_atten", "[amp]*gaus(x, [mean], [sigma])", -8.0, 8.0);
		ff_atten.setParameter(0, h_atten_compare.getBinContent(h_atten_compare.getMaximumBin()));
		ff_atten.setParameter(1, h_atten_compare.getMean());
		ff_atten.setParameter(2, h_atten_compare.getRMS());
		ff_atten.setLineColor(2);
		ff_atten.setLineWidth(2);
		
		DataFitter.fit(ff_atten, h_atten_compare, "RNQ");
		
		h_atten_compare.setLineWidth(2);
		h_atten_compare.setOptStat("1101001110");
		h_atten_compare.getStatBox().setFontSize(18);
		
		can.cd(0);
		//can.draw(g_atten_ideal);
		//can.draw(g_atten_smeared, "same");
		can.draw(g_atten_smeared);
		can.draw(g_atten_extracted, "same");
		can.draw(f_atten_ideal, "same");
		
		can.cd(1);
		can.draw(h_atten_compare);
		

		/// veff ///
		CcdbData data_veff_ideal = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 11, new Date(), "/calibration/ftof/effective_velocity");
		CcdbData data_veff_smeared = new CcdbData("sqlite:///../../../clas12.smeared_constants_final.sqlite", "default", 17, new Date(), "/calibration/ftof/effective_velocity");
		CcdbData data_veff_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(), "/calibration/ftof/effective_velocity");

		GraphErrors g_veff_ideal = cc.getColumnGraphErrors(data_veff_ideal, 3, -1, 1, 2);
		GraphErrors g_veff_smeared = cc.getColumnGraphErrors(data_veff_smeared, 3, -1, 1, 2);
		GraphErrors g_veff_extracted = cc.getColumnGraphErrors(data_veff_extracted, 3, -1, 1, 2);
		
		g_veff_ideal.setMarkerColor(1);
		g_veff_ideal.setTitle("FTOF 1B veff (sec 1)");
		g_veff_ideal.setTitleX("paddle");
		g_veff_ideal.setTitleY("cm/ns");
		g_veff_smeared.setMarkerColor(2);
		g_veff_smeared.setTitle("FTOF 1B veff (sec 1)");
		g_veff_smeared.setTitleX("paddle");
		g_veff_smeared.setTitleY("cm/ns");
		g_veff_extracted.setMarkerColor(3);
		g_veff_extracted.setMarkerSize(4);
		g_veff_extracted.setTitle("FTOF 1B veff (sec 1)");
		g_veff_extracted.setTitleX("paddle");
		g_veff_extracted.setTitleY("cm/ns");
		
		F1D f_veff_ideal = new F1D("f_veff_ideal", "[b] + [m]*x", 0, 63);
		f_veff_ideal.setParameter(0, 16.0);
		f_veff_ideal.setParameter(1, 0.0);
		f_veff_ideal.setLineColor(1);
		f_veff_ideal.setLineWidth(2);

		H1F h_veff_compare = cc.compareDiffPercentH1D(data_veff_smeared, data_veff_extracted, 3, 0, 0, 25, -8.0, 8.0);
		h_veff_compare.setTitle("FTOF 1B veff (sec 1)");
		h_veff_compare.setTitleX("percent difference (%)");
		
		F1D ff_veff = new F1D("ff_veff", "[amp]*gaus(x, [mean], [sigma])", -8.0, 8.0);
		ff_veff.setParameter(0, h_veff_compare.getBinContent(h_veff_compare.getMaximumBin()));
		ff_veff.setParameter(1, h_veff_compare.getMean());
		ff_veff.setParameter(2, h_veff_compare.getRMS());
		ff_veff.setLineColor(2);
		ff_veff.setLineWidth(2);
		
		DataFitter.fit(ff_veff, h_veff_compare, "RNQ");
		
		h_veff_compare.setLineWidth(2);
		h_veff_compare.setOptStat("1101001110");
		h_veff_compare.getStatBox().setFontSize(18);
		
		can.cd(2);
		//can.draw(g_veff_ideal);
		//can.draw(g_veff_smeared, "same");
		can.draw(g_veff_smeared);
		can.draw(g_veff_extracted, "same");
		can.draw(f_veff_ideal, "same");
		
		can.cd(3);
		can.draw(h_veff_compare);	
		
		/// left_right timing offset ///
		CcdbData data_LR_ideal = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 11, new Date(), "/calibration/ftof/timing_offset");
		CcdbData data_LR_smeared = new CcdbData("sqlite:///../../../clas12.smeared_constants_final.sqlite", "default", 17, new Date(), "/calibration/ftof/timing_offset");
		CcdbData data_LR_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(), "/calibration/ftof/timing_offset");

		GraphErrors g_LR_ideal = cc.getColumnGraphErrors(data_LR_ideal, 3, -1, 1, 2);
		GraphErrors g_LR_smeared = cc.getColumnGraphErrors(data_LR_smeared, 3, -1, 1, 2);
		GraphErrors g_LR_extracted = cc.getColumnGraphErrors(data_LR_extracted, 3, -1, 1, 2);
		
		g_LR_ideal.setMarkerColor(1);
		g_LR_ideal.setTitle("FTOF 1B LR (sec 1)");
		g_LR_ideal.setTitleX("paddle");
		g_LR_ideal.setTitleY("ns");
		g_LR_smeared.setMarkerColor(2);
		g_LR_smeared.setTitle("FTOF 1B LR (sec 1)");
		g_LR_smeared.setTitleX("paddle");
		g_LR_smeared.setTitleY("ns");
		g_LR_extracted.setMarkerColor(3);
		g_LR_extracted.setMarkerSize(4);
		g_LR_extracted.setTitle("FTOF 1B LR (sec 1)");
		g_LR_extracted.setTitleX("paddle");
		g_LR_extracted.setTitleY("ns");
		
		F1D f_LR_ideal = new F1D("f_LR_ideal", "[b] + [m]*x", 0, 63);
		f_LR_ideal.setParameter(0, 0.0);
		f_LR_ideal.setParameter(1, 0.0);
		f_LR_ideal.setLineColor(1);
		f_LR_ideal.setLineWidth(2);

		H1F h_LR_compare = cc.compareDiffH1D(data_LR_smeared, data_LR_extracted, 3, 0, 0, 25, -0.5, 0.5);
		h_LR_compare.setTitle("FTOF 1B LR (sec 1)");
		h_LR_compare.setTitleX("difference (ns)");
		
		F1D ff_LR = new F1D("ff_LR", "[amp]*gaus(x, [mean], [sigma])", -0.5, 0.5);
		ff_LR.setParameter(0, h_LR_compare.getBinContent(h_LR_compare.getMaximumBin()));
		ff_LR.setParameter(1, h_LR_compare.getMean());
		ff_LR.setParameter(2, h_LR_compare.getRMS());
		ff_LR.setLineColor(2);
		ff_LR.setLineWidth(2);
		
		DataFitter.fit(ff_LR, h_LR_compare, "RNQ");
		
		h_LR_compare.setLineWidth(2);
		h_LR_compare.setOptStat("1101001110");
		h_LR_compare.getStatBox().setFontSize(18);
		
		can.cd(4);
		//can.draw(g_LR_ideal);
		//can.draw(g_LR_smeared, "same");
		can.draw(g_LR_smeared);
		can.draw(g_LR_extracted, "same");
		can.draw(f_LR_ideal, "same");
		
		can.cd(5);
		can.draw(h_LR_compare);	
	
	}

}
