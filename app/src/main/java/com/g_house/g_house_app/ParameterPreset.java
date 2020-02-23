package com.g_house.g_house_app;

public class ParameterPreset {
    private String name;
    private String pH_thresh_low;
    private String pH_thresh_high;
    private String EC_thresh_low;

    public ParameterPreset(String name, float ph_lo, float ph_hi, float ec_lo)
    {
        this.name = name;
        this.pH_thresh_low = String.format("%.2f", ph_lo);
        this.pH_thresh_high = String.format("%.2f", ph_hi);
        this.EC_thresh_low = String.format("%.2f", ec_lo);
    }

    public String getName() { return name; }
    public String getPHLow() { return pH_thresh_low; }
    public String getPHHigh() { return pH_thresh_high; }
    public String getECLow() { return EC_thresh_low; }
}
