package com.rylinaux.plugman.command;

/*
 * #%L
 * PlugMan
 * %%
 * Copyright (C) 2010 - 2014 PlugMan
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.rylinaux.plugman.PlugMan;
import com.rylinaux.plugman.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.naming.directory.InvalidSearchFilterException;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Command that installs plugin(s) from Spigot repositories.
 *
 * @author turpster
 */
public class InstallCommand extends AbstractCommand {

    /**
     * The location of the PlugMan cache.
     */
    public static final String PLUGMAN_CACHE_URL = PlugMan.getInstance().getDataFolder() + "/cache";

    /**
     * The name of the command.
     */
    public static final String NAME = "install";

    /**
     * The description of the command.
     */
    public static final String DESCRIPTION = "Install a plugin.";

    /**
     * The main permission of the command.
     */
    public static final String PERMISSION = "plugman.install";

    /**
     * The proper usage of the command.
     */
    public static final String USAGE = "/plugman install <plugin>";

    /**
     * The sub permissions of the command.
     */
    public static final String[] SUB_PERMISSIONS = {""};

    /**
     * Construct out object.
     *
     * @param sender the command sender
     */
    public InstallCommand(CommandSender sender) {
        super(sender, NAME, DESCRIPTION, PERMISSION, SUB_PERMISSIONS, USAGE);
    }

    /**
     * Execute the command.
     *
     * @param sender  the sender of the command
     * @param command the command being done
     * @param label   the name of the command
     * @param args    the arguments supplied
     */
    @Override
    public void execute(final CommandSender sender, Command command, final String label, final String[] args) {
        if (!hasPermission()) {
            sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("error.no-permission"));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("error.specify-plugin"));
            sendUsage();
            return;
        }

        final long id = SpiGetUtil.getPluginId(args[1]);

        if (id == -1) {
            sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("install.not-found"));
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {

                final HttpDownload pluginDownload = new HttpDownload(SpiGetUtil.API_BASE_URL + "resources/" + id + "/download", PLUGMAN_CACHE_URL);

//                final BukkitTask progressBarTask = new BukkitRunnable() {
//
//                    ASCIIProgressBar progressBar = new ASCIIProgressBar();
//
//                    @Override
//                    public void run() {
//
//                        float currentPercentage = pluginDownload.getPercentage();
//
//                        progressBar.setPercentage(currentPercentage);
//
//                        // TODO Check if sender disconnected through the middle of the pluginDownload.
//
//                        sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("install.installing.downloading", (((float) Math.round(currentPercentage * 10000)) / 100) + "%", args[1]));
//                        sender.sendMessage(progressBar.getProgressBar((short) 54));
//                        if (currentPercentage >= 1.0) {
//                            sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("install.installing.downloaded", (((float) Math.round(currentPercentage * 10000)) / 100) + "%", args[1]));
//
//                            this.cancel();
//                        }
//                    }
//                }.runTaskTimerAsynchronously(PlugMan.getInstance(), 0, 20);

                File cacheFile = null;

                try {
                    cacheFile = pluginDownload.download();
                } catch (FileAlreadyExistsException e) {
                    cacheFile = new File(pluginDownload.getTargetLocation());
                } catch (IOException e) {
                    sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("install.error-installing", args[1]));
                    e.printStackTrace();
                }

                ArrayList<File> loadPlugins = new ArrayList<>();

                FileUtil.FileType cacheFileType = FileUtil.getFileType(cacheFile.getName());

                if (cacheFileType == FileUtil.FileType.ZIP_FILE) {
                    ZipFile zipFile = null;

                    try {
                        zipFile = new ZipFile(cacheFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    ArrayList<ZipEntry> zipJarEntries = new ArrayList<>();

                    Enumeration<? extends ZipEntry> entries = zipFile.entries();
                    while(entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();

                        if (StringUtils.endsWithIgnoreCase(entry.getName(), ".jar")) {
                            zipJarEntries.add(entry);
                        }
                    }

                    if (args.length >= 3) {

                        String[] fileSelectionsStr = args[2].split(",");
                        int[] fileSelections = new int[fileSelectionsStr.length];

                        for (int i = 0; i < fileSelections.length; i++)
                        {
                            fileSelections[i] = Integer.parseInt(fileSelectionsStr[i]);
                        }

                        for (int selection : fileSelections) {
                            String zipFilePath = zipJarEntries.get(selection).getName();

                            File plugin = new File("plugins/" + zipFilePath.substring(zipFilePath.lastIndexOf("/")));

                            if (plugin.isFile()) {
                                sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("error.jar-file-exists", plugin.getName()));
                                return;
                            }

                            BufferedInputStream zipJarInputStream = null;
                            BufferedOutputStream pluginOutputStream = null;

                            try {
                                zipJarInputStream = new BufferedInputStream(zipFile.getInputStream(zipJarEntries.get(selection)));

                                plugin.createNewFile();
                                pluginOutputStream = new BufferedOutputStream(new FileOutputStream(plugin));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            int inByte;
                            try {
                                while ((inByte = zipJarInputStream.read()) != -1) {
                                    pluginOutputStream.write(inByte);
                                }

                                zipJarInputStream.close();
                                pluginOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            loadPlugins.add(plugin);
                        }
                    }
                    else {
                        sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("install.zip-file.select-jar", cacheFile.getName(), label + StringUtil.consolidateStrings(args, 0)));

                        StringBuilder jarFileSelection = new StringBuilder();

                        for (int i = 0; i < zipJarEntries.size(); i++) {
                            jarFileSelection.append("(").append(i).append(") ").append(zipJarEntries.get(i).getName()).append("\n");
                        }

                        sender.sendMessage(jarFileSelection.toString());
                    }
                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (cacheFileType == FileUtil.FileType.JAR_FILE) {
                    try
                    {
                        FileUtils.copyFileToDirectory(cacheFile, new File("plugins"));
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    loadPlugins.add(new File("plugins/" + cacheFile.getName()));
                }

                for (File file : loadPlugins)
                {
                    try {
                        PluginUtil.enable(PluginUtil.load(file.getName()));

                    } catch (InvalidSearchFilterException | InvalidDescriptionException | InvalidPluginException | NotDirectoryException e) {
                        sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("install.plugin-failed"));
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(PlugMan.getInstance());
    }
}