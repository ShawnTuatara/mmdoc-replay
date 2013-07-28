package ca.tuatara.mmdoc.replay.ui;

import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import ca.tuatara.mmdoc.replay.data.Replay;

public class EloGraph extends JFrame {
    private static final long serialVersionUID = 1L;

    public EloGraph(List<Replay> replays) {
        super("Duel of Champions - Elo History");

        TimeSeries eloSeries = new TimeSeries("Elo");
        for (Replay replay : replays) {
            eloSeries.add(new Second(replay.getDatePlayed()), replay.getPlayerElo());
        }

        TimeSeriesCollection seriesCollection = new TimeSeriesCollection(eloSeries);

        JFreeChart eloChart = ChartFactory.createTimeSeriesChart("Elo History", "Date", "Elo", seriesCollection, false, true, false);
        XYPlot xyPlot = eloChart.getXYPlot();
        DateAxis dateAxis = (DateAxis) xyPlot.getDomainAxis();
        dateAxis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM-dd"));

        ChartPanel chartPanel = new ChartPanel(eloChart);
        chartPanel.setPreferredSize(new java.awt.Dimension(1000, 500));
        setContentPane(chartPanel);
    }
}
