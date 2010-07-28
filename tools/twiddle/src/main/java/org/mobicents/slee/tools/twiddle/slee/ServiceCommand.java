/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.mobicents.slee.tools.twiddle.slee;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.PrintWriter;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.slee.ServiceID;
import javax.slee.management.ServiceState;

import org.jboss.console.twiddle.command.CommandContext;
import org.jboss.console.twiddle.command.CommandException;
import org.jboss.logging.Logger;
import org.mobicents.slee.tools.twiddle.AbstractSleeCommand;
import org.mobicents.slee.tools.twiddle.JMXNameUtility;
import org.mobicents.slee.tools.twiddle.Operation;

/**
 * Command for service interaction - activate/deactivate etc.
 * 
 * @author baranowb
 * 
 */
public class ServiceCommand extends AbstractSleeCommand {



	public ServiceCommand() {
		super("service", "Performs operation on JSLEE ServiceManagementMBean.");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.slee.tools.twiddle.AbstractSleeCommand#displayHelp()
	 */
	@Override
	public void displayHelp() {
		PrintWriter out = context.getWriter();

		out.println(desc);
		out.println();
		out.println("usage: " + name + " <operation> <arg>*");
		out.println();
		out.println("operation:");
		out.println("    -a, --activate                 Activates service('s) with matching ServiceID.");
		out.println("                                   Requiers atleast one ServiceID as argument.");
		out.println("    -d, --deactivate               Deactivates service('s) with matching ServiceID");
		out.println("                                   Requiers atleast one ServiceID as argument.");
		out.println("    -c, --deactivate-and-activate  Deactivates and activates service('s) with matching ServiceID(s)");
		out.println("                                   Supports two sub options(mandatory):");
		out.println("                       --ta        Indicates services to be activated in this operation.");
		out.println("                       --td        Indicates services to be deactivated in this operation.");
		out.println("    -u, --usage-mbean              Returns the Object Name of a ServiceUsageMBean object");
		out.println("                                   Requiers ServiceID as argument.");
		out.println("    -i, --services                 Returns list of services in given state");
		out.println("                                   Requiers ServiceState as argument.");
		out.println("    -o, --state                    Returns state of service");
		out.println("                                   Requiers ServiceID as argument.");
		out.println();
		out.println("arg:");
		out.println("    ServiceID:             ServiceID[name=xxx,vendor=uuu,version=123.0.00]");
		out.println("    ServiceID Array:       ServiceID[name=xxx,vendor=uuu,version=123.0.00];ServiceID[name=xxx,vendor=uuu,version=123.0.00]");
		out.println("    ServiceState:          [Active,Inactive,Stopping]  ");
		out.flush();
	}

	

	/* (non-Javadoc)
	 * @see org.mobicents.slee.tools.twiddle.AbstractSleeCommand#getBeanOName()
	 */
	@Override
	public ObjectName getBeanOName() throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(JMXNameUtility.SLEE_SERVICE_MANAGEMENT);
	}

	protected void processArguments(String[] args) throws CommandException {

		String sopts = ":a:d:u:i:o:c"; // ":" is for req, argument, lack of it
										// after option means no args.

		LongOpt[] lopts = { new LongOpt("activate", LongOpt.REQUIRED_ARGUMENT, null, 'a'),
				new LongOpt("deactivate", LongOpt.REQUIRED_ARGUMENT, null, 'd'),
				new LongOpt("usage-mbean", LongOpt.REQUIRED_ARGUMENT, null, 'u'),
				new LongOpt("services", LongOpt.REQUIRED_ARGUMENT, null, 'i'),
				new LongOpt("state", LongOpt.REQUIRED_ARGUMENT, null, 'o'),
				new LongOpt("deactivate-and-activate", LongOpt.NO_ARGUMENT, null, 'c'),
				// Longopts for deactivateAndActivate - no short ops for those
					new LongOpt("ta", LongOpt.REQUIRED_ARGUMENT, null, DeactivateAndActivateOperation.ta),
					new LongOpt("td", LongOpt.REQUIRED_ARGUMENT, null, DeactivateAndActivateOperation.td) };

		Getopt getopt = new Getopt(null, args, sopts, lopts);
		// getopt.setOpterr(false);

		int code;
		while ((code = getopt.getopt()) != -1) {
			switch (code) {
			case ':':
				throw new CommandException("Option requires an argument: " + args[getopt.getOptind() - 1]);

			case '?':
				throw new CommandException("Invalid (or ambiguous) option: " + args[getopt.getOptind() - 1]);

			case 'a':
				// operationName = "activate";
				super.operation = new ActivateOperation(super.context, super.log, this);
				super.operation.buildOperation(getopt, args);
				break;
			case 'd':
				// operationName = "deactivate";
				super.operation = new DeactivateOperation(super.context, super.log, this);
				super.operation.buildOperation(getopt, args);
				break;
			case 'c':
				// operationName = "deactivateAndActivate";
				// this requires some more args than one, so...
				super.operation = new DeactivateAndActivateOperation(super.context, super.log, this);
				super.operation.buildOperation(getopt, args);

				break;
			case 'u':
				// operationName = "getServiceUsageMBean";
				super.operation = new GetServiceUsageMBeanOperation(super.context, super.log, this);
				super.operation.buildOperation(getopt, args);
				break;
			case 'i':
				// operationName = "getServices";
				super.operation = new GetServicesOperation(super.context, super.log, this);
				super.operation.buildOperation(getopt, args);

				break;
			case 'o':
				// operationName = "getState";
				super.operation = new GetStateOperation(super.context, super.log, this);
				super.operation.buildOperation(getopt, args);

				break;
			default:
				throw new CommandException("Command: \"" + getName() + "\", found unexpected opt: " + args[getopt.getOptind() - 1]);

			}
		}
	}

	private class ActivateOperation extends Operation {

		public ActivateOperation(CommandContext context, Logger log, ServiceCommand serviceCommand) {
			super(context, log, serviceCommand);
			this.operationName = "activate";
		}

		@Override
		public void buildOperation(Getopt opts, String[] args) throws CommandException {
			String optArg = opts.getOptarg();

			try {
				if (optArg.contains(";")) {
					// arrays for ServiceID
					addArg(optArg, ServiceID[].class, true);

				} else {
					addArg(optArg, ServiceID.class, true);
				}
			} catch (Exception e) {
				throw new CommandException("Failed to parse ServiceID: \"" + optArg + "\"", e);
			}

		}

	}

	private class DeactivateOperation extends Operation {

		public DeactivateOperation(CommandContext context, Logger log, ServiceCommand serviceCommand) {
			super(context, log, serviceCommand);
			this.operationName = "deactivate";
		}

		@Override
		public void buildOperation(Getopt opts, String[] args) throws CommandException {
			String optArg = opts.getOptarg();

			try {
				if (optArg.contains(";")) {
					// arrays for ServiceID
					addArg(optArg, ServiceID[].class, true);

				} else {
					addArg(optArg, ServiceID.class, true);
				}
			} catch (Exception e) {
				throw new CommandException("Failed to parse ServiceID: \"" + optArg + "\"", e);
			}

		}

	}

	private class GetServiceUsageMBeanOperation extends Operation {

		public GetServiceUsageMBeanOperation(CommandContext context, Logger log, ServiceCommand serviceCommand) {
			super(context, log, serviceCommand);
			this.operationName = "getServiceUsageMBean";
		}

		@Override
		public void buildOperation(Getopt opts, String[] args) throws CommandException {
			String optArg = opts.getOptarg();

			if (optArg.contains(";")) {
				throw new CommandException("Option does not support array argument.");

			} else {
				try {
					addArg(optArg, ServiceID.class, true);
				} catch (Exception e) {
					throw new CommandException("Failed to parse ServiceID: \"" + optArg + "\"", e);
				}
			}

		}

	}

	private class GetServicesOperation extends Operation {

		public GetServicesOperation(CommandContext context, Logger log, ServiceCommand serviceCommand) {
			super(context, log, serviceCommand);
			this.operationName = "getServices";
		}

		@Override
		public void buildOperation(Getopt opts, String[] args) throws CommandException {
			String optArg = opts.getOptarg();

			if (optArg.contains(";")) {
				throw new CommandException("Option does not support array argument.");

			} else {
				try {
					addArg(optArg, ServiceState.class, true);
				} catch (Exception e) {
					throw new CommandException("Failed to parse ServiceState: \"" + optArg + "\"", e);
				}
			}

		}

	}

	private class GetStateOperation extends Operation {

		public GetStateOperation(CommandContext context, Logger log, ServiceCommand serviceCommand) {
			super(context, log, serviceCommand);
			this.operationName = "getState";
		}

		@Override
		public void buildOperation(Getopt opts, String[] args) throws CommandException {
			String optArg = opts.getOptarg();

			if (optArg.contains(";")) {
				throw new CommandException("Option does not support array argument.");

			} else {
				try {
					addArg(optArg, ServiceID.class, true);
				} catch (Exception e) {
					throw new CommandException("Failed to parse ServiceID: \"" + optArg + "\"", e);
				}
			}

		}

	}

	private class DeactivateAndActivateOperation extends Operation {
		// long opts for that
		public static final char ta = 'z';
		public static final char td = 'x';
		private String toActivate;
		private String toDeactivate;

		public DeactivateAndActivateOperation(CommandContext context, Logger log, ServiceCommand serviceCommand) {
			super(context, log, serviceCommand);
			this.operationName = "deactivateAndActivate";
		}

		@Override
		public void buildOperation(Getopt opts, String[] args) throws CommandException {
			// next two opts must contain conf for this.
			int code;
			while ((code = opts.getopt()) != -1) {
				switch (code) {
				case ':':
					throw new CommandException("Option requires an argument: " + args[opts.getOptind() - 1]);

				case '?':
					throw new CommandException("Invalid (or ambiguous) option: " + args[opts.getOptind() - 1] + " --> " + opts.getOptopt());

				case ta:
					toActivate = opts.getOptarg();
					break;
				case td:
					toDeactivate = opts.getOptarg();
					break;
				default:
					throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
							+ "\", found unexpected opt: " + args[opts.getOptind() - 1]);

				}
			}
			if (toActivate == null || toDeactivate == null) {
				throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
						+ "\", requires set of sub options!");
			}

			Class argClass = ServiceID.class;
			if (toActivate.contains(";") || toDeactivate.contains(";")) {
				argClass = ServiceID[].class;
			}
			try {
				addArg(toDeactivate, argClass, true);
			} catch (Exception e) {
				throw new CommandException("Failed to parse service IDs to deactivate: \"" + toDeactivate + "\"", e);
			}

			try {
				addArg(toActivate, argClass, true);
			} catch (Exception e) {
				throw new CommandException("Failed to parse service IDs to activate: \"" + toActivate + "\"", e);
			}

		}

	}
	
}
