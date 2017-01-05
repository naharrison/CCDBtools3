package ccdbTools;

import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.GraphErrors;
import org.jlab.groot.data.H1F;
import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.math.F1D;
import org.jlab.groot.ui.TCanvas;

import java.util.Date;

public class CTOFAnalysis {
	public static void main(String[] args) {
		GStyle.getAxisAttributesX().setTitleFontSize(18);
		GStyle.getAxisAttributesX().setLabelFontSize(18);
		GStyle.getAxisAttributesY().setTitleFontSize(18);
		GStyle.getAxisAttributesY().setLabelFontSize(18);

		ConstantsComparer cc = new ConstantsComparer();

		TCanvas can = new TCanvas("can", 900, 850);
		can.divide(2, 3);
		can.getCanvas().setTitleSize(18);
		can.getCanvas().getPad(0).getAxisY().setRange(80, 220);
		can.getCanvas().getPad(2).getAxisY().setRange(5, 25);
		can.getCanvas().getPad(4).getAxisY().setRange(-15, 15);
		
		/// attenuation length ///
		CcdbData data_atten_ideal = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 11, new Date(), "/calibration/ctof/attenuation");
		CcdbData data_atten_smeared = new CcdbData("sqlite:///../../../clas12.smeared_constants_final.sqlite", "default", 17, new Date(), "/calibration/ctof/attenuation");
		CcdbData data_atten_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(), "/calibration/ctof/attenuation");

		GraphErrors g_atten_ideal = cc.getColumnGraphErrors(data_atten_ideal, 3, -1, 0, 0);
		GraphErrors g_atten_smeared = cc.getColumnGraphErrors(data_atten_smeared, 3, -1, 0, 0);
		GraphErrors g_atten_extracted = cc.getColumnGraphErrors(data_atten_extracted, 3, -1, 0, 0);
		
		g_atten_ideal.setMarkerColor(1);
		g_atten_ideal.setTitle("CTOF atten. length");
		g_atten_ideal.setTitleX("paddle");
		g_atten_ideal.setTitleY("cm");
		g_atten_smeared.setMarkerColor(2);
		g_atten_smeared.setTitle("CTOF atten. length");
		g_atten_smeared.setTitleX("paddle");
		g_atten_smeared.setTitleY("cm");
		g_atten_extracted.setMarkerColor(3);
		g_atten_extracted.setMarkerSize(4);
		g_atten_extracted.setTitle("CTOF atten. length");
		g_atten_extracted.setTitleX("paddle");
		g_atten_extracted.setTitleY("cm");
		
		F1D f_atten_ideal = new F1D("f_atten_ideal", "[b] + [m]*x", 0, 49);
		f_atten_ideal.setParameter(0, 140.0);
		f_atten_ideal.setParameter(1, 0.0);
		f_atten_ideal.setLineColor(1);
		f_atten_ideal.setLineWidth(2);

		H1F h_atten_compare = cc.compareDiffPercentH1D(data_atten_smeared, data_atten_extracted, 3, 0, 0, 25, -20.0, 20.0);
		h_atten_compare.setTitle("CTOF atten. length");
		h_atten_compare.setTitleX("percent difference (%)");
		
		F1D ff_atten = new F1D("ff_atten", "[amp]*gaus(x, [mean], [sigma])", -20.0, 20.0);
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
		CcdbData data_veff_ideal = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 11, new Date(), "/calibration/ctof/effective_velocity");
		CcdbData data_veff_smeared = new CcdbData("sqlite:///../../../clas12.smeared_constants_final.sqlite", "default", 17, new Date(), "/calibration/ctof/effective_velocity");
		CcdbData data_veff_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(), "/calibration/ctof/effective_velocity");

		GraphErrors g_veff_ideal = cc.getColumnGraphErrors(data_veff_ideal, 3, -1, 0, 0);
		GraphErrors g_veff_smeared = cc.getColumnGraphErrors(data_veff_smeared, 3, -1, 0, 0);
		GraphErrors g_veff_extracted = cc.getColumnGraphErrors(data_veff_extracted, 3, -1, 0, 0);
		
		g_veff_ideal.setMarkerColor(1);
		g_veff_ideal.setTitle("CTOF veff");
		g_veff_ideal.setTitleX("paddle");
		g_veff_ideal.setTitleY("cm/ns");
		g_veff_smeared.setMarkerColor(2);
		g_veff_smeared.setTitle("CTOF veff");
		g_veff_smeared.setTitleX("paddle");
		g_veff_smeared.setTitleY("cm/ns");
		g_veff_extracted.setMarkerColor(3);
		g_veff_extracted.setMarkerSize(4);
		g_veff_extracted.setTitle("CTOF veff");
		g_veff_extracted.setTitleX("paddle");
		g_veff_extracted.setTitleY("cm/ns");
		
		F1D f_veff_ideal = new F1D("f_veff_ideal", "[b] + [m]*x", 0, 49);
		f_veff_ideal.setParameter(0, 16.0);
		f_veff_ideal.setParameter(1, 0.0);
		f_veff_ideal.setLineColor(1);
		f_veff_ideal.setLineWidth(2);

		H1F h_veff_compare = cc.compareDiffPercentH1D(data_veff_smeared, data_veff_extracted, 3, 0, 0, 25, -20.0, 20.0);
		h_veff_compare.setTitle("CTOF veff");
		h_veff_compare.setTitleX("percent difference (%)");
		
		F1D ff_veff = new F1D("ff_veff", "[amp]*gaus(x, [mean], [sigma])", -20.0, 20.0);
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
		
		/// up_down timing offset ///
		CcdbData data_UD_ideal = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 11, new Date(), "/calibration/ctof/timing_offset");
		CcdbData data_UD_smeared = new CcdbData("sqlite:///../../../clas12.smeared_constants_final.sqlite", "default", 17, new Date(), "/calibration/ctof/timing_offset");
		CcdbData data_UD_extracted = new CcdbData("mysql://clas12reader@clasdb.jlab.org/clas12", "default", 17, new Date(), "/calibration/ctof/timing_offset");

		GraphErrors g_UD_ideal = cc.getColumnGraphErrors(data_UD_ideal, 3, -1, 0, 0);
		GraphErrors g_UD_smeared = cc.getColumnGraphErrors(data_UD_smeared, 3, -1, 0, 0);
		GraphErrors g_UD_extracted = cc.getColumnGraphErrors(data_UD_extracted, 3, -1, 0, 0);
		
		g_UD_ideal.setMarkerColor(1);
		g_UD_ideal.setTitle("CTOF UD");
		g_UD_ideal.setTitleX("paddle");
		g_UD_ideal.setTitleY("ns");
		g_UD_smeared.setMarkerColor(2);
		g_UD_smeared.setTitle("CTOF UD");
		g_UD_smeared.setTitleX("paddle");
		g_UD_smeared.setTitleY("ns");
		g_UD_extracted.setMarkerColor(3);
		g_UD_extracted.setMarkerSize(4);
		g_UD_extracted.setTitle("CTOF UD");
		g_UD_extracted.setTitleX("paddle");
		g_UD_extracted.setTitleY("ns");
		
		F1D f_UD_ideal = new F1D("f_UD_ideal", "[b] + [m]*x", 0, 49);
		f_UD_ideal.setParameter(0, 0.0);
		f_UD_ideal.setParameter(1, 0.0);
		f_UD_ideal.setLineColor(1);
		f_UD_ideal.setLineWidth(2);

		H1F h_UD_compare = cc.compareDiffH1D(data_UD_smeared, data_UD_extracted, 3, 0, 0, 25, -10.0, 10.0);
		h_UD_compare.setTitle("CTOF UD");
		h_UD_compare.setTitleX("difference (ns)");
		
		F1D ff_UD = new F1D("ff_UD", "[amp]*gaus(x, [mean], [sigma])", -10.0, 10.0);
		ff_UD.setParameter(0, h_UD_compare.getBinContent(h_UD_compare.getMaximumBin()));
		ff_UD.setParameter(1, h_UD_compare.getMean());
		ff_UD.setParameter(2, h_UD_compare.getRMS());
		ff_UD.setLineColor(2);
		ff_UD.setLineWidth(2);
		
		DataFitter.fit(ff_UD, h_UD_compare, "RNQ");
		
		h_UD_compare.setLineWidth(2);
		h_UD_compare.setOptStat("1101001110");
		h_UD_compare.getStatBox().setFontSize(18);
		
		can.cd(4);
		//can.draw(g_UD_ideal);
		//can.draw(g_UD_smeared, "same");
		can.draw(g_UD_smeared);
		can.draw(g_UD_extracted, "same");
		can.draw(f_UD_ideal, "same");
		
		can.cd(5);
		can.draw(h_UD_compare);	
	
	}

}
