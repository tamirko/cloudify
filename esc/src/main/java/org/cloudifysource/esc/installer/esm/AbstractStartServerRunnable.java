/*******************************************************************************
 * Copyright (c) 2011 GigaSpaces Technologies Ltd. All rights reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.cloudifysource.esc.installer.esm;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.cloudifysource.esc.esm.CloudMachineProvisioningConfig;
import org.cloudifysource.esc.installer.AgentlessInstaller;
import org.cloudifysource.esc.installer.InstallationDetails;
import org.cloudifysource.esc.installer.InstallerException;
import org.cloudifysource.esc.util.Utils;


import com.gigaspaces.internal.utils.StringUtils;

/**************
 * A base class for implementations of the start server task used by the
 * AbstractMachineProvisioning. The implementation delegates the actual
 * provisioning and booting of the server to the inheriting class via
 * startServer(), and then the agentless installer takes over and sets up the
 * required files and processes on the remote machine.
 * 
 * @author barakme
 * 
 */
public abstract class AbstractStartServerRunnable {

	private static final java.util.logging.Logger logger = java.util.logging.Logger
			.getLogger(AbstractStartServerRunnable.class.getName());
	private final AgentlessInstaller installer;
	private final InstallationDetails details;

	/*********
	 * Constructor.
	 * 
	 * @param installer
	 *            The agentless installer.
	 * @param details
	 *            the installation details.
	 * @throws FileNotFoundException 
	 */
	public AbstractStartServerRunnable(final AgentlessInstaller installer, final InstallationDetails details) throws FileNotFoundException {
		super();
		this.installer = installer;
		this.details = details;
	}

	public void run(int numberOfMachines, final long timeout, final TimeUnit unit) throws InstallerException, InterruptedException,
			TimeoutException {
		if (timeout < 0) {
			throw new TimeoutException("Starting a new machine timed out");
		}
		final long end = System.currentTimeMillis() + unit.toMillis(timeout);
		
		logger.fine("Start Server task has started executing!");
		
		final InstallationDetails[] installationDetails = startServers(numberOfMachines, timeout, unit);

		installer.installOnMachineWithIP(installationDetails[0], Utils.millisUntil(end), TimeUnit.MILLISECONDS);
	}

	/*************
	 * Starts a new server, physical or virtual. The server should have *-nix
	 * operating system and ssh available on port 22.
	 * 
	 * @param unit
	 * @param timeout
	 * 
	 * @return The IP for the new server.
	 * @throws InstallerException
	 *             If a server could not be started.
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	protected abstract InstallationDetails[] startServers(int numberOfMachines, long timeout, TimeUnit unit) throws InstallerException, InterruptedException,
			TimeoutException;

	protected AgentlessInstaller getInstaller() {
		return installer;
	}

	protected InstallationDetails getDetails() {
		return details;
	}
	
    protected static InstallationDetails createInstallationDetails(
            final CloudMachineProvisioningConfig config) throws FileNotFoundException {
        final InstallationDetails details = new InstallationDetails();
        details.setLocalDir(config.getLocalDirectory());
        details.setZones(StringUtils.join(config.getGridServiceAgentZones(), 
				",", 0, config.getGridServiceAgentZones().length));
        details.setRemoteDir(config.getRemoteDirectory());
        details.setLocator(null);
        details.setPrivateIp(null);
        details.setLus(true);
        details.setCloudifyUrl(config.getCloudifyUrl());
        details.setConnectedToPrivateIp(config.isConnectedToPrivateIp());
        if ((config.getKeyPair() != null) && (config.getKeyPair().length() > 0)) {
            File keyFile = new File(config.getKeyFile());
        	if (!keyFile.isAbsolute()) {
	        	keyFile = new File(details.getLocalDir(), config.getKeyFile());
            }
            if (!keyFile.isFile()) {
                throw new FileNotFoundException("keyfile : "
                        + keyFile.getAbsolutePath() + " not found");
            }
            details.setKeyFile(keyFile.getAbsolutePath());
        }
        details.setPassword(config.getSshPassword());
        details.setUsername(config.getSshUsername());
        return details;
    }
	
}
