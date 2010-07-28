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
import java.util.Arrays;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.slee.profile.ProfileSpecificationID;

import org.jboss.console.twiddle.command.CommandContext;
import org.jboss.console.twiddle.command.CommandException;
import org.jboss.logging.Logger;
import org.mobicents.slee.tools.twiddle.AbstractSleeCommand;
import org.mobicents.slee.tools.twiddle.JMXNameUtility;
import org.mobicents.slee.tools.twiddle.Operation;

/**
 * Command which accesses Profile MBean.
 * @author baranowb
 *
 */
public class ProfileCommand extends AbstractSleeCommand {

	/**
	 * @param name
	 * @param desc
	 */
	public ProfileCommand() {
		super("profile",  "This command performs operations on JSLEE ProfileProvisioningMBean." ); //FIXME: find way to add getprofilesBy...
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
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
		out.println("    -l, --list                     Lists components based on passed suboption:");
		out.println("            --table                Lists profile table names, if ProfileSpecificationID is passed as argument, ");
		out.println("                                   names listed are only for tables which ProfileSpecificationID matches.");
		out.println("            --profile              Lists profile IDs by table name,");
		out.println("                                   requiers profile table name as argument.");
		out.println("    -c, --create                   Creates component based on passed suboptions. Suported are two sets [ \"--table-name\" & \"--profile-name\" | \"--table-name\" & \"--profile-spec\" ]");
		out.println("            --profile-name         Indicates profile name to be created. It is used in conjuction with \"--table-name\" to create profile in table.");
		out.println("            --table-name           Indicates profile table name. It reqquiers either \"--profile-name\" or \"--profile-spec\".");
		out.println("            --profile-spec         Indicates ProfileSpecification ID used to craete table.");
		out.println("    -r, --remove                   Removes component based on passed suboptions. Requiers atleast \"--table-name\" suboption. Following suboptions are supported:");
		out.println("            --table-name           Indicates table name to be removed. If \"--profile-name\" is also used, only profile is removed from table.");
		out.println("            --profile-name         Indicates profile name of profile to be removed. It is used in conjuction with \"--table-name\".");
		out.println("    -n, --rename                   Renames profile table, based on suboptions. Both are required. Supported suboptions are:");
		out.println("            --current-name         Sets current name of profile table.");
		out.println("            --new-name             Sets new name for profile table.");
		//its weird, other MBeans define methods like getXXXUsage or something...
		out.println("    -g, --get                      Fetches information regarding profiles and profile tables based on suboption(one). Supported suboptions:");
		out.println("            --profile-spec         Retrieves ProfileSpecification ID for given table name,");
		out.println("                                   requiers profile table name as argument.");
		out.println("            --profile              Retrieves ObjectName of default profile for given table name,");
		out.println("                                   requiers profile table name as argument. It can be used in conjuction with \"--profile-name\" ");
		out.println("                                   to get ObjectName for specific profile. Side effect of this call is registration of MBean with return ObjectName");
		out.println("            --profile-name         Sets name for ObjectName fetch operation to make \"--profile\" option get ObjectName for specific profile.");
		out.println("                                   Requiers profile name as argument.");
		out.println("arg:");

		
		out.flush();

	}

	

	/* (non-Javadoc)
	 * @see org.mobicents.slee.tools.twiddle.AbstractSleeCommand#processArguments(java.lang.String[])
	 */
	@Override
	protected void processArguments(String[] args) throws CommandException {
		String sopts = ":lcrng";
		
		LongOpt[] lopts = { 
				new LongOpt("list", LongOpt.NO_ARGUMENT, null, 'l'),
					new LongOpt("table", LongOpt.OPTIONAL_ARGUMENT, null, ListOperation.table),
					new LongOpt("profile", LongOpt.REQUIRED_ARGUMENT, null, ListOperation.profile),
				new LongOpt("create", LongOpt.NO_ARGUMENT, null, 'c'),
					//those are also for remove
					new LongOpt("profile-name", LongOpt.REQUIRED_ARGUMENT, null, CreateOperation.profile_name),
					new LongOpt("table-name", LongOpt.REQUIRED_ARGUMENT, null, CreateOperation.table_name),
					new LongOpt("profile-spec", LongOpt.REQUIRED_ARGUMENT, null, CreateOperation.profile_spec),
				new LongOpt("remove", LongOpt.NO_ARGUMENT, null, 'r'),
					//covered above
				new LongOpt("rename", LongOpt.NO_ARGUMENT, null, 'n'),
					new LongOpt("current-name", LongOpt.REQUIRED_ARGUMENT, null, RenameOperation.current_name),
					new LongOpt("new-name", LongOpt.REQUIRED_ARGUMENT, null, RenameOperation.new_name),
				new LongOpt("get", LongOpt.NO_ARGUMENT, null, 'g'),
					new LongOpt("profile-name", LongOpt.REQUIRED_ARGUMENT, null, GetOperation.profile_name),
					new LongOpt("profile-spec", LongOpt.REQUIRED_ARGUMENT, null, GetOperation.profile_spec),
					new LongOpt("profile", LongOpt.REQUIRED_ARGUMENT, null, GetOperation.profile),
				};

		Getopt getopt = new Getopt(null, args, sopts, lopts);
		getopt.setOpterr(false);

		int code;
		while ((code = getopt.getopt()) != -1) {
			switch (code) {
			case ':':
				throw new CommandException("Option requires an argument: " + args[getopt.getOptind() - 1]);

			case '?':
				throw new CommandException("Invalid (or ambiguous) option: " + args[getopt.getOptind() - 1]);

			case 'l':

				super.operation = new ListOperation(super.context, super.log, this);
				super.operation.buildOperation(getopt, args);

				break;
			case 'c':

				super.operation = new CreateOperation(super.context, super.log, this);
				super.operation.buildOperation(getopt, args);

				break;
			case 'r':

				super.operation = new RemoveOperation(super.context, super.log, this);
				super.operation.buildOperation(getopt, args);

				break;
			case 'n':

				super.operation = new RenameOperation(super.context, super.log, this);
				super.operation.buildOperation(getopt, args);

				break;
			case 'g':

				super.operation = new GetOperation(super.context, super.log, this);
				super.operation.buildOperation(getopt, args);

				break;
			

			
			default:
				throw new CommandException("Command: \"" + getName() + "\", found unexpected opt: " + args[getopt.getOptind() - 1]);

			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.slee.tools.twiddle.AbstractSleeCommand#getBeanOName()
	 */
	@Override
	public ObjectName getBeanOName() throws MalformedObjectNameException, NullPointerException {
		return new ObjectName(JMXNameUtility.SLEE_PROFILE_PROVISIONING);
	}

	private class ListOperation extends Operation {
		public static final char table = 'o';
		public static final char profile = 'p';

		public ListOperation(CommandContext context, Logger log, AbstractSleeCommand sleeCommand) {
			super(context, log, sleeCommand);
			// operation is not set. its used to determine if more options were
			// passed.
		}

		@Override
		public void buildOperation(Getopt opts, String[] args) throws CommandException {
			int code;
			String optArg;
			while ((code = opts.getopt()) != -1) {
				if (super.operationName != null) {
					throw new CommandException("Command: \"" + sleeCommand.getName() + "\", expects either \"--table\" or \"--profile\"!");
				}
				switch (code) {
				case ':':
					throw new CommandException("Option requires an argument: " + args[opts.getOptind() - 1]);

				case '?':
					throw new CommandException("Invalid (or ambiguous) option: " + args[opts.getOptind() - 1]);

				case table:

					super.operationName = "getProfileTables";
					optArg = opts.getOptarg();

					if (optArg != null) {
						// it must be ProfileSpecificationID
						try {
							addArg(optArg, ProfileSpecificationID.class, true);
						} catch (Exception e) {
							throw new CommandException("Failed to parse ProfileSpecificationID: \"" + optArg + "\"", e);
						}
					}
					break;
				case profile:

					super.operationName = "getProfiles";
					optArg = opts.getOptarg();
					addArg(optArg, String.class, false); // table name
					break;

				default:
					throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
							+ "\", found unexpected opt: " + args[opts.getOptind() - 1]);

				}
				if (super.operationName == null) {
					throw new CommandException("Command: \"" + sleeCommand.getName() + "\", expects either \"--table\" or \"--profile\"!");
				}

			}

		}
	}

	private class CreateOperation extends Operation {
		public static final char table_name = 'b';
		public static final char profile_name = 'v';
		public static final char profile_spec = 'm';

		private String stringTableName;
		private String stringProfileName;
		private String stringProfileSpec;

		public CreateOperation(CommandContext context, Logger log, AbstractSleeCommand sleeCommand) {
			super(context, log, sleeCommand);
			// operation is not set. its used to determine if more options were
			// passed.
		}

		@Override
		public void buildOperation(Getopt opts, String[] args) throws CommandException {
			int code;
			while ((code = opts.getopt()) != -1) {
				switch (code) {
				case ':':
					throw new CommandException("Option requires an argument: " + args[opts.getOptind() - 1]);

				case '?':
					throw new CommandException("Invalid (or ambiguous) option: " + args[opts.getOptind() - 1]);

				case table_name:

					this.stringTableName = opts.getOptarg();

					break;
				case profile_name:

					this.stringProfileName = opts.getOptarg();

					break;
				case profile_spec:

					this.stringProfileSpec = opts.getOptarg();

					break;

				default:
					throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
							+ "\", found unexpected opt: " + args[opts.getOptind() - 1]);

				}

			}

			if ((this.stringProfileSpec == null && this.stringProfileName == null)) {
				throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
						+ "\", expects either \"--profile-name\" or \"--profile-spec\" to be present");
			}
			if ((this.stringProfileSpec != null && this.stringProfileName != null)) {
				throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
						+ "\", expects either \"--profile-name\" or \"--profile-spec\" to be present");
			}
			if (this.stringTableName == null) {
				throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
						+ "\", expects \"--profile-table\" to be present");
			}
			if (this.stringProfileName != null) {
				// create profile
				super.operationName = "createProfile";
				super.addArg(this.stringTableName, String.class, false);
				super.addArg(this.stringProfileName, String.class, false);
			} else {
				super.operationName = "createProfileTable";
				try {
					super.addArg(this.stringProfileSpec, ProfileSpecificationID.class, true);
				} catch (Exception e) {
					throw new CommandException("Failed to parse ProfileSpecificationID: \"" + stringProfileSpec + "\"", e);
				}
				super.addArg(this.stringTableName, String.class, false);
			}

		}
	}

	private class RemoveOperation extends Operation {
		public static final char table_name = 'b';
		public static final char profile_name = 'v';

		private String stringTableName;
		private String stringProfileName;

		public RemoveOperation(CommandContext context, Logger log, AbstractSleeCommand sleeCommand) {
			super(context, log, sleeCommand);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void buildOperation(Getopt opts, String[] args) throws CommandException {
			int code;

			while ((code = opts.getopt()) != -1) {
				switch (code) {
				case ':':
					throw new CommandException("Option requires an argument: " + args[opts.getOptind() - 1]);

				case '?':
					throw new CommandException("Invalid (or ambiguous) option: " + args[opts.getOptind() - 1]);

				case table_name:

					this.stringTableName = opts.getOptarg();
					break;
				case profile_name:

					this.stringProfileName = opts.getOptarg();
					break;

				default:
					throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
							+ "\", found unexpected opt: " + args[opts.getOptind() - 1]);

				}

			}
			if (this.stringTableName == null) {
				throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
						+ "\", expects \"--profile-table\" to be present");
			}
			super.addArg(this.stringTableName, String.class, false);
			if (this.stringProfileName == null) {
				super.operationName = "removeProfileTable";
			} else {
				super.operationName = "removeProfile";
				super.addArg(this.stringProfileName, String.class, false);
			}
		}
	}

	private class RenameOperation extends Operation {
		public static final char current_name = 'k';
		public static final char new_name = 'j';

		private String stringCurrentTableName;
		private String stringNewTableName;

		public RenameOperation(CommandContext context, Logger log, AbstractSleeCommand sleeCommand) {
			super(context, log, sleeCommand);
			super.operationName = "renameProfileTable";
		}

		@Override
		public void buildOperation(Getopt opts, String[] args) throws CommandException {
			int code;

			while ((code = opts.getopt()) != -1) {
				switch (code) {
				case ':':
					throw new CommandException("Option requires an argument: " + args[opts.getOptind() - 1]);

				case '?':
					throw new CommandException("Invalid (or ambiguous) option: " + args[opts.getOptind() - 1]);

				case current_name:

					this.stringCurrentTableName = opts.getOptarg();
					break;
				case new_name:

					this.stringNewTableName = opts.getOptarg();
					break;

				default:
					throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
							+ "\", found unexpected opt: " + args[opts.getOptind() - 1]);

				}
			}
			if (this.stringCurrentTableName == null && this.stringNewTableName == null) {
				throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
						+ "\", expects both \"--current-name\" and \"--new-spec\" to be present");
			}
			super.addArg(this.stringCurrentTableName, String.class, false);
			super.addArg(this.stringNewTableName, String.class, false);

		}
	}

	private class GetOperation extends Operation {
		public static final char profile_name = 'v';
		public static final char profile_spec = 'm';
		public static final char profile = 'x';
		// tricky.. ech
		private String stringProfileSpec_TableName; // holds table name for
		// getProfileSpecification(String
		// tableName); .. others
		// similar
		private String stringProfile_TableName;
		private String stringProfileName_Name;

		public GetOperation(CommandContext context, Logger log, AbstractSleeCommand sleeCommand) {
			super(context, log, sleeCommand);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void buildOperation(Getopt opts, String[] args) throws CommandException {
			int code;

			while ((code = opts.getopt()) != -1) {
				switch (code) {
				case ':':
					throw new CommandException("Option requires an argument: " + args[opts.getOptind() - 1]);

				case '?':
					throw new CommandException("Invalid (or ambiguous) option: " + args[opts.getOptind() - 1]);

				case profile_name:

					this.stringProfileName_Name = opts.getOptarg();
					break;
				case profile_spec:

					this.stringProfileSpec_TableName = opts.getOptarg();
					break;
				case profile:

					this.stringProfile_TableName = opts.getOptarg();
					break;

				default:
					throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
							+ "\", found unexpected opt: " + args[opts.getOptind() - 1]);

				}

			}

			if ((this.stringProfileSpec_TableName == null && this.stringProfile_TableName == null)
					|| (this.stringProfileSpec_TableName != null && this.stringProfile_TableName != null)) {
				throw new CommandException("Operation \"" + this.operationName + "\" for command: \"" + sleeCommand.getName()
						+ "\", expects either \"--profile\" or \"--profile-spec\" to be present");
			}
			if (this.stringProfileSpec_TableName != null) {
				super.operationName = "getProfileSpecification";
				super.addArg(stringProfileSpec_TableName, String.class, false);
			} else {
				// its get other stuff.
				if (this.stringProfileName_Name == null) {
					// default
					super.operationName = "getDefaultProfile";
					super.addArg(stringProfile_TableName, String.class, false);
				} else {
					super.operationName = "getProfile";
					super.addArg(stringProfile_TableName, String.class, false);
					super.addArg(stringProfileName_Name, String.class, false);
				}
			}

		}
	}
}
