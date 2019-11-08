package ru.sberbank.adminconsole;

import java.awt.Container;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import ru.sberbank.adminconsole.gui.panels.MainPane;
import ru.sberbank.adminconsole.gui.services.AuthorizationService;
import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.model.configuration.ApplicationConfiguration;
import ru.sberbank.adminconsole.model.configuration.ModuleConfiguration;
import ru.sberbank.adminconsole.services.IConfigurationLoader;
import ru.sberbank.adminconsole.services.impl.ConfigurationLoaderImpl;

public class Main {

    public static void addComponentsToPane(Container pane) {
    	pane.add(new MainPane());        
 
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
    	
    	try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
        //Create and set up the window.
        JFrame frame = new JFrame("Admin console");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(200, 70);
        
        //Set up the content pane.
        addComponentsToPane(frame.getContentPane());

        //Display the window.
        frame.pack();
        AuthorizationService.showAuthorizationWindow(frame);
        
        frame.setVisible(true);        
    }

    public static void main(String[] args) {    
		IConfigurationLoader confLoader = new ConfigurationLoaderImpl();

		ApplicationConfiguration appConfiguration = confLoader.readApplicationConfiguration();
		ModuleConfiguration moduleConfiguration = confLoader.readModuleConfiguration();
		
		if (appConfiguration == null) {
			JOptionPane.showMessageDialog(null, "Can't read configuration.xml", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} else
			ClientDataManager.getInstance().setServers(appConfiguration.getApplications());			
			
		if (moduleConfiguration == null) {
			JOptionPane.showMessageDialog(null, "Can't read modules confugiration", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} else
			ClientDataManager.getInstance().setSystemModules(moduleConfiguration.getModules());			

		
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
