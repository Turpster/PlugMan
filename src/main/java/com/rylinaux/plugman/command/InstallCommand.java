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

                File cacheDownloadFile = null;

                try {
                    cacheDownloadFile = pluginDownload.download();
                } catch (FileAlreadyExistsException e) {
                    cacheDownloadFile = new File(pluginDownload.getTargetLocation());
                } catch (IOException e) {
                    sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("install.error-installing", args[1]));
                    e.printStackTrace();
                }

                ArrayList<File> plugins = new ArrayList<>();

                if (StringUtils.endsWithIgnoreCase(cacheDownloadFile.getName(), ".zip")) {
                    ZipFile zipFile = null;
                    try {
                        zipFile = new ZipFile(cacheDownloadFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Enumeration<? extends ZipEntry> entries = zipFile.entries();

                    System.out.println(entries);

                    ArrayList<ZipEntry> fileNumberBindings = new ArrayList<>();

                    while(entries.hasMoreElements()) {
                        ZipEntry entry = entries.nextElement();

                        if (StringUtils.endsWithIgnoreCase(entry.getName(), ".jar")) {
                            fileNumberBindings.add(entry);
                        }
                    }

                    if (args.length >= 3) {
                        ArrayList<Integer> selections = new ArrayList<>();

                        for (String number : args[2].split(",")) {
                            try {
                                selections.add(Integer.parseInt(number));
                            } catch (NumberFormatException e) {
                                sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("error.number-format-exception", number));
                                return;
                            }
                        }

                        for (int selection : selections) {
                            String zipFilePath = fileNumberBindings.get(selection).getName();

                            File targetLocation = new File("plugins/" + zipFilePath.substring(zipFilePath.lastIndexOf("/")));

                            System.out.println(zipFilePath);

                            if (targetLocation.isFile()) {
                                sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("error.jar-file-exists", targetLocation.getName()));
                                return;
                            }

                            BufferedInputStream inputStream = null;
                            try {
                                inputStream = new BufferedInputStream(zipFile.getInputStream(fileNumberBindings.get(selection)));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            try {
                                targetLocation.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            BufferedOutputStream bufferedOutputStream = null;
                            try {
                                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(targetLocation));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            int inByte;
                            try {
                                while ((inByte = inputStream.read()) != -1) {
                                    bufferedOutputStream.write(inByte);
                                }

                                inputStream.close();
                                bufferedOutputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            plugins.add(targetLocation);
                        }
                    }
                    else {
                        sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("install.zip-file.select-jar", cacheDownloadFile.getName(), label + StringUtil.consolidateStrings(args, 0)));

                        StringBuilder numberBindings = new StringBuilder();

                        for (int i = 0; i < fileNumberBindings.size(); i++) {
                            numberBindings.append("(").append(i).append(") ").append(fileNumberBindings.get(i).getName()).append(" ");
                        }

                        sender.sendMessage(numberBindings.toString());
                    }
                    try {
                        zipFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (StringUtils.endsWithIgnoreCase(cacheDownloadFile.getAbsolutePath(), ".jar")) {
                    try
                    {
                        FileUtils.copyFileToDirectory(cacheDownloadFile, new File("plugins"));
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }

                    plugins.add(new File("plugins/" + cacheDownloadFile.getName()));
                }

                for (File file : plugins)
                {
                    try {
                        PluginUtil.load(file.getName());
                    } catch (InvalidSearchFilterException | InvalidDescriptionException | InvalidPluginException | NotDirectoryException e) {
                        sender.sendMessage(PlugMan.getInstance().getMessageFormatter().format("install.plugin-failed"));
                        e.printStackTrace();
                    }
                }
            }
        }.runTaskAsynchronously(PlugMan.getInstance());
    }
}