package com.viaoa.jfcapp;

/**
 * Types of commands to added to JFC toolbar.
 * @author vvia
 */
public class ToolBarOptions {
    public boolean bGoBack;
    public boolean bIcon;
    public boolean bLabel;
    public boolean bGoto;
    public boolean bCommands;
    public boolean bSearch;
    public boolean bFind;
    public boolean bCustom;
    public boolean bHubCalc;
    public boolean bReport;
    public boolean bTable;
    public boolean bHubSearch;
    public boolean bDownload;

    
    public ToolBarOptions() {
    }
    
    
    // bGoBack bIcon bLabel bGoto bCommands bSearch bFind bCustom bHubCalc bReport bTable bHubSearch bDownload
    public ToolBarOptions(boolean bGoBack, boolean bIcon, boolean bLabel, boolean bGoto, boolean bCommands, boolean bSearch,
            boolean bFind, boolean bCustom, boolean bHubCalc, boolean bReport, boolean bTable, boolean bHubSearch, boolean bDownload) {
        this.bGoBack = bGoBack;
        this.bIcon = bIcon;
        this.bLabel = bLabel;
        this.bGoto = bGoto;
        this.bCommands = bCommands;
        this.bSearch = bSearch;
        this.bFind = bFind;
        this.bCustom = bCustom;
        this.bHubCalc = bHubCalc;
        this.bReport = bReport;
        this.bTable = bTable;
        this.bHubSearch = bHubSearch;
        this.bDownload = bDownload;
    }
    
    public static ToolBarOptions createToolBar() {
        // bGoBack bIcon bLabel bGoto bCommands bSearch bFind bCustom bHubCalc bReport bTable bHubSearch bDownload
        ToolBarOptions tbo = new ToolBarOptions(true, true, true, true, true, true, true, true, true, true, true, true, true);
        return tbo;
    }
    public static ToolBarOptions createMainToolBar() {
        // bGoBack bIcon bLabel bGoto bCommands bSearch bFind bCustom bHubCalc bReport bTable bHubSearch bDownload
        ToolBarOptions tbo = new ToolBarOptions(false, true, false, false, false, false, true, false, false, true, true, false, false);
        return tbo;
    }
    public static ToolBarOptions createTableToolBar() {
        // bGoBack bIcon bLabel bGoto bCommands bSearch bFind bCustom bHubCalc bReport bTable bHubSearch bDownload
        ToolBarOptions tbo = new ToolBarOptions(false, false, false, true, true, true, true, true, false, true, true, true, true);
        return tbo;
    }

    public static ToolBarOptions createNorthTableSearchToolBar() {
        // bGoBack bIcon bLabel bGoto bCommands bSearch bFind bCustom bHubCalc bReport bTable bHubSearch bDownload
        ToolBarOptions tbo = new ToolBarOptions(false, true, false, false, false, true, true, false, false, true, true, false, false);
        return tbo;
    }

    public static ToolBarOptions createSouthTableSearchToolBar() {
        // bGoBack bIcon bLabel bGoto bCommands bSearch bFind bCustom bHubCalc bReport bTable bHubSearch bDownload
        ToolBarOptions tbo = new ToolBarOptions(false, false, false, true, true, false, false, true, true, false, false, false, true);
        return tbo;
    }

    public static ToolBarOptions createTableFindToolBar() {
        // bGoBack bIcon bLabel bGoto bCommands bSearch bFind bCustom bHubCalc bReport bTable bHubSearch bDownload
        ToolBarOptions tbo = new ToolBarOptions(false, false, false, false, false, true, true, false, false, true, true, false, false);
        return tbo;
    }

    public static ToolBarOptions createNorthTableSearchOnlyToolBar() {
        // bGoBack bIcon bLabel bGoto bCommands bSearch bFind bCustom bHubCalc bReport bTable bHubSearch bDownload
        ToolBarOptions tbo = new ToolBarOptions(false, false, false, false, false, true, false, false, false, true, true, false, false);
        return tbo;
    }

    public static ToolBarOptions createSouthTableToolBar() {
        // bGoBack bIcon bLabel bGoto bCommands bSearch bFind bCustom bHubCalc bReport bTable bHubSearch bDownload
        ToolBarOptions tbo = new ToolBarOptions(false, false, false, true, true, false, false, true, true, false, false, true, true);
        return tbo;
    }

    public static ToolBarOptions createEditPanelToolBar() {
        // bGoBack bIcon bLabel bGoto bCommands bSearch bFind bCustom bHubCalc bReport bTable bHubSearch bDownload
        ToolBarOptions tbo = new ToolBarOptions(true, true, false, false, true, false, false, true, false, true, false, false, false);
        return tbo;
    }

    public static ToolBarOptions createDialogToolBar() {
        // bGoBack bIcon bLabel bGoto bCommands bSearch bFind bCustom bHubCalc bReport bTable bHubSearch bDownload
        ToolBarOptions tbo = new ToolBarOptions(false, false, true, false, true, false, false, true, false, true, false, false, false);
        return tbo;
    }

    public static ToolBarOptions createOneToolBar() {
        // bGoBack bIcon bLabel bGoto bCommands bSearch bFind bCustom bHubCalc bReport bTable bHubSearch bDownload
        ToolBarOptions tbo = new ToolBarOptions(false, false, false, false, true, false, false, true, true, true, false, false, false);
        return tbo;
    }

}
