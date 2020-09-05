package au.edu.qut.prom.helpers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UIExportPlugin;
import org.processmining.contexts.uitopia.annotations.UIImportPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.contexts.uitopia.packagemanager.PMController;
import org.processmining.contexts.uitopia.packagemanager.PMPackage;
import org.processmining.framework.boot.Boot;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.util.HTMLToString;
import org.processmining.plugins.ShowPackageOverviewPlugin;

/**
 * Partially working package and plugin listing. The package listing works, the plugin listing 
 * doesn't. Based on <code>org.processmining.plugins.ShowPackageOverviewPlugin</code> in prom core
 * plugins package. Idea was to output more info available for help etc, but it's an experiment 
 * that didn't work out. For now, just run PromM and run the "Show Package Overview" plugin. You
 * can then copy/paste the output into a spreadsheet, like an animal.
 *  
 * @author burkeat
 *
 */
public class PackageListingExporter {

	
	private static String toExtendedHTMLString(Collection<PluginDescriptor> pluginDescriptors, 
			boolean includeHTMLTags) 
	{
		StringBuffer buffer = new StringBuffer();

		if (includeHTMLTags) {
			buffer.append("<html>");
		}
		buffer.append("<h1>ProM Package Overview</h1>");

		PMController packageController = new PMController(Boot.Level.NONE);
		List<? extends PMPackage> uptodatePackages = packageController.getToUninstallPackages();
		List<? extends PMPackage> outofdatePackages = packageController.getToUpdatePackages();
		buffer.append("<h2>Installed packages</h2>");
		packageListAsTable(buffer, uptodatePackages);
		buffer.append("<h3>Updates available</h3>");
		packageListAsTable(buffer, outofdatePackages);

		buffer.append("<h2>Available plug-ins</h2>");
		buffer.append("<table>");
		buffer.append("<tr><th>Plug-in name</th><th>UITopia</th><th>UITopia name</th><th>Package name</th><th>Author name</th><th>Description</th><th>Help</th></tr>");
		for (PluginDescriptor pluginDescriptor : pluginDescriptors) {
			String uiName = null;
			boolean isUITopia = false;
			UITopiaVariant variant = pluginDescriptor.getAnnotation(UITopiaVariant.class);
			if (variant != null) {
				uiName = variant.uiLabel();
				isUITopia = true;
				variantAsRow(buffer, pluginDescriptor, uiName, variant);
			}
			Visualizer visualizer = pluginDescriptor.getAnnotation(Visualizer.class);
			if (visualizer != null) {
				uiName = visualizer.name();
				isUITopia = true;
				visualizerAsRow(buffer, pluginDescriptor, uiName);
			}
			UIImportPlugin importPlugin = pluginDescriptor.getAnnotation(UIImportPlugin.class);
			if (importPlugin != null) {
				uiName = pluginDescriptor.getName();
				isUITopia = true;
				importPluginAsRow(buffer, pluginDescriptor, uiName);
			}
			UIExportPlugin exportPlugin = pluginDescriptor.getAnnotation(UIExportPlugin.class);
			if (exportPlugin != null) {
				uiName = pluginDescriptor.getName();
				isUITopia = true;
				uiExportPluginAsRow(buffer, pluginDescriptor, uiName);
			}
			for (int i = 0; i < pluginDescriptor.getNumberOfMethods(); i++) {
				variant = pluginDescriptor.getAnnotation(UITopiaVariant.class, i);
				if (variant != null) {
					uiName = variant.uiLabel();
					isUITopia = true;
					variantAsRow(buffer, pluginDescriptor, uiName, variant);
				}
			}
			if (!isUITopia) {
				nonUITopiaRow(buffer, pluginDescriptor, uiName);
			}
		}
		buffer.append("</table>");
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		return buffer.toString();

	}


	private static void nonUITopiaRow(StringBuffer buffer, PluginDescriptor pluginDescriptor, String uiName) {
		buffer.append("<tr>");
		buffer.append("<td>" + pluginDescriptor.getName() + "</td>");
		buffer.append("<td></td>");
		buffer.append("<td>" + (uiName == null ? "" : uiName) + "</td>");
		packageCells(buffer, pluginDescriptor);
	}


	private static void packageCells(StringBuffer buffer, PluginDescriptor pluginDescriptor) {
		String packName = null;
		String authorName = null;
		String description = null;
		PackageDescriptor packageDescriptor = pluginDescriptor.getPackage();
		if (packageDescriptor != null) {
			packName = packageDescriptor.getName();
			authorName = packageDescriptor.getAuthor();
			description = packageDescriptor.getDescription();
		}
		buffer.append("<td>" + (packName == null ? "" : packName) + "</td>");
		buffer.append("<td>" + (authorName == null ? "" : authorName) + "</td>");
		cell( (description == null ? "" : description), buffer );
		buffer.append("</tr>");
	}


	private static void uiExportPluginAsRow(StringBuffer buffer, PluginDescriptor pluginDescriptor, String uiName) {
		buffer.append("<tr>");
		buffer.append("<td>" + pluginDescriptor.getName() + "</td>");
		buffer.append("<td>Export</td>");
		buffer.append("<td>" + (uiName == null ? "" : uiName) + "</td>");
		packageCells(buffer, pluginDescriptor);
	}


	private static void importPluginAsRow(StringBuffer buffer, PluginDescriptor pluginDescriptor, String uiName) {
		buffer.append("<tr>");
		buffer.append("<td>" + pluginDescriptor.getName() + "</td>");
		buffer.append("<td>Import</td>");
		buffer.append("<td>" + (uiName == null ? "" : uiName) + "</td>");
		packageCells(buffer, pluginDescriptor);
	}


	private static void visualizerAsRow(StringBuffer buffer, PluginDescriptor pluginDescriptor, String uiName) {
		buffer.append("<tr>");
		buffer.append("<td>" + pluginDescriptor.getName() + "</td>");
		buffer.append("<td>Visualizer</td>");
		buffer.append("<td>" + (uiName == null ? "" : uiName) + "</td>");
		packageCells(buffer, pluginDescriptor);
	}


	private static void variantAsRow(StringBuffer buffer, PluginDescriptor pluginDescriptor, String uiName,
			UITopiaVariant variant) {
		buffer.append("<tr>");
		buffer.append("<td>" + pluginDescriptor.getName() + "</td>");
		buffer.append("<td>Plug-in variant</td>");
		buffer.append("<td>" + (uiName == null ? "" : uiName) + "</td>");
		String packName = null;
		PackageDescriptor packageDescriptor = pluginDescriptor.getPackage();
		if (packageDescriptor != null) {
			packName = packageDescriptor.getName();
		}
		buffer.append("<td>" + (packName == null ? "" : packName) + "</td>");
		buffer.append("<td>" + variant.author() + "</td>");
		cell( variant.uiHelp() , buffer);
		buffer.append("</tr>");
	}

	private static void packageListAsTable(StringBuffer buffer, List<? extends PMPackage> uptodatePackages) {
		buffer.append("<table>");
		buffer.append("<tr><th>Package</th><th>Dependency</th><th>Version</th><th>Author</th><th>Description</th></tr>");
		for (PMPackage pack : uptodatePackages) {
			buffer.append("<tr>");
			buffer.append("<td>" + pack.getPackageName() + "</td>");
			buffer.append("<td></td>");
			buffer.append("<td>" + pack.getVersion() + "</td>");
			buffer.append("<td>" + pack.getAuthorName() + "</td>");
			cell(pack.getDescription(),buffer);
			buffer.append("</tr>");
			for (String s : pack.getDependencies()) {
				buffer.append("<tr><td></td><td>" + s + "</td><td></td><td></td></tr>");
			}
		}
		buffer.append("</table>");
	}
	
	private static void cell(String contents, StringBuffer buffer) {
		buffer.append("<td>" + contents + "</td>");
	}

	public static void standardListing(String fileName) throws IOException{
		FileWriter writer = new FileWriter(fileName);
		UIPluginContext uipc = 
				new HeadlessUIPluginContext(new ConsoleUIPluginContext(), "show_package_exporter");
		HTMLToString output = ShowPackageOverviewPlugin.info(uipc); 
		writer.write(output.toHTMLString(true));
		writer.close();
	}

	public static void extendedListing(String fileName) throws IOException{
		FileWriter writer = new FileWriter(fileName);
		UIPluginContext uipc = 
				new HeadlessUIPluginContext(new ConsoleUIPluginContext(), "show_package_exporter");
		System.out.println("extendedListing()");
		PackageManager manager = PackageManager.getInstance();
		manager.initialize(Boot.Level.ALL);
		Set<PackageDescriptor> packages = manager.getAvailablePackages();
		System.out.println("Total packages:" + packages.size());		
		SortedSet<PluginDescriptor> allPlugins = uipc.getPluginManager().getAllPlugins();
		System.out.println("Total plugins:" + allPlugins.size());	
		// TODO: at time of writing, no plugins are initialized, so this list is empty
		// which takes away the most useful aspect of this little tool. 
		writer.write(toExtendedHTMLString(allPlugins, true));
		writer.close();
	}
	
	public static void main(String[] args) throws IOException{
		// standardListing("prompackages.html");
		extendedListing("prompackagesextended.html");
	}
}
