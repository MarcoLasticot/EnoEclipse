package org.enoeclipse.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;

import org.enoeclipse.exception.EnoEclipseException;
import org.enoeclipse.utils.EnoEclipseLogger;

import com.matrixone.apps.domain.util.MqlUtil;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.db.Program;
import matrix.db.ProgramItr;
import matrix.db.ProgramList;
import matrix.util.MatrixException;

public class EnoTreeProgram extends EnoTreeBusiness implements IAttributable, ITriggerable {
	Program program;
	protected String description;
	protected boolean hidden;
	protected String code;
	protected String programType;
	protected boolean immediate;
	protected boolean needsBusinessObject;
	protected boolean downloadable;
	protected boolean piped;
	protected boolean pooled;
	protected EnoTreePerson user;
	protected static ArrayList<EnoTreeProgram> allPrograms;
	protected static String MQL_INFO = "print program \"{0}\" select description hidden execute user dump |;";
	protected static String MQL_MODIFY_DIRECTION_INFO = "modify relationship \"{0}\" {1} cardinality {2} revision {3} clone {4}  {5}  {6};";
	protected static final int INFO_DESCRIPTION = 0;
	protected static final int INFO_HIDDEN = 1;
	protected static final int INFO_EXECUTE = 2;
	protected static final int INFO_USER = 3;
	public static String JPO_SUFFIX = "_mxJPO";
	public static final String PROGRAM_TYPE_JAVA = "java";
	public static final String PROGRAM_TYPE_MQL = "mql";
	public static final String PROGRAM_TYPE_EXTERNAL = "external";
	public static final String[] PROGRAM_TYPES = { "java", "mql", "external" };
	public static final String EXECUTE_IMMEDIATE = "immediate";
	public static final String EXECUTE_DEFERRED = "deferred";
	public static final String NEEDS_BUSINESS_OBJECT = "needsbusinessobject";
	public static final String DOWNLOADABLE = "downloadable";
	public static final String PIPED = "pipe";
	public static final String POOLED = "pooled";

	public EnoTreeProgram(String name) throws EnoEclipseException, MatrixException {
		super("Program", name);
		boolean bFound = false;
		for (EnoTreeProgram p : getAllPrograms(false)) {
			if (name.equals(p.getName())) {
				this.program = p.program;
				bFound = true;
				break;
			}
		}
		if (!bFound) {
			throw new EnoEclipseException("Program " + name + " not found in ENOVIA!");
		}
	}
	
	public static Boolean exists(String name) throws EnoEclipseException, MatrixException {
		boolean bFound = false;
		for (EnoTreeProgram p : getAllPrograms(false)) {
			if (name.equals(p.getName())) {
				bFound = true;
				break;
			}
		}
		return bFound;
	}

	protected EnoTreeProgram(Program program) throws EnoEclipseException, MatrixException {
		super("Program", program.getName());
		this.program = program;
	}

	public void refresh() throws EnoEclipseException, MatrixException {
		super.refresh();
		fillBasics();
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isHidden() {
		return this.hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getProgramType() {
		return this.programType;
	}

	public void setProgramType(String programType) {
		this.programType = programType;
	}

	public boolean isDownloadable() {
		return this.downloadable;
	}

	public void setDownloadable(boolean downloadable) {
		this.downloadable = downloadable;
	}

	public boolean isImmediate() {
		return this.immediate;
	}

	public void setImmediate(boolean immediate) {
		this.immediate = immediate;
	}

	public boolean isNeedsBusinessObject() {
		return this.needsBusinessObject;
	}

	public void setNeedsBusinessObject(boolean needsBusinessObject) {
		this.needsBusinessObject = needsBusinessObject;
	}

	public boolean isPiped() {
		return this.piped;
	}

	public void setPiped(boolean piped) {
		this.piped = piped;
	}

	public boolean isPooled() {
		return this.pooled;
	}

	public void setPooled(boolean pooled) {
		this.pooled = pooled;
	}

	public EnoTreePerson getUser() {
		return this.user;
	}

	public void setUser(EnoTreePerson user) {
		this.user = user;
	}

	public static ArrayList<EnoTreeProgram> getAllPrograms(boolean refresh) throws MatrixException, EnoEclipseException {
		if ((refresh) || (allPrograms == null)) {
			Context context = getContext();
			ProgramList rtl = Program.getPrograms(context, true);
			allPrograms = new ArrayList<EnoTreeProgram>();
			ProgramItr rti = new ProgramItr(rtl);
			while (rti.next()) {
				Program rt = rti.obj();
				EnoTreeProgram rel = new EnoTreeProgram(rt);
				allPrograms.add(rel);
			}
			Collections.sort(allPrograms);
		}
		return allPrograms;
	}

	public static String[] getAllProgramNames(boolean refresh) throws MatrixException, EnoEclipseException {
		ArrayList<EnoTreeProgram> allPrograms = getAllPrograms(refresh);

		String[] retVal = new String[allPrograms.size()];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = ((EnoTreeProgram)allPrograms.get(i)).getName();
		}
		return retVal;
	}

	public void fillBasics() {
		try {
			Context context = getContext();
			this.program.open(context);
			try {
				this.name = this.program.getName();

				MQLCommand command = new MQLCommand();
				command.executeCommand(context, MessageFormat.format(MQL_INFO, new Object[] { this.name }));

				String[] info = command.getResult().trim().split("\\|");
				this.description = info[0];
				this.hidden = info[1].equalsIgnoreCase("true");
				this.programType = (this.program.isMqlProgram() ? "mql" : this.program.isJavaProgram() ? "java" : "external");
				this.immediate = info[2].equals("immediate");
				fillAdditional(this);
				if (info.length > 3) {
					this.user = new EnoTreePerson(info[3]);
				} else {
					this.user = null;
				}

				this.code = this.program.getCode(context);
			} finally {
				this.program.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public static void fillAdditional(EnoTreeProgram program) {
		try {
			//MQLCommand command = new MQLCommand();
			Context context = getContext();
			//command.executeCommand(context, MessageFormat.format(MQL_SIMPLE_PRINT, new Object[] { program.getType().toLowerCase(), program.getName() }));
			String[] lines = MqlUtil.mqlCommand(context, MQL_SIMPLE_PRINT_NEW, program.getType().toLowerCase(), program.getName()).split("\n");

			program.needsBusinessObject = false;
			program.downloadable = false;
			program.piped = false;
			program.pooled = false;

			for (int i = 0; i < lines.length; i++) {
				lines[i] = lines[i].trim();
				if (lines[i].equals("needsbusinessobject")) {
					program.needsBusinessObject = true;
				} else if (lines[i].equals("downloadable")) {
					program.downloadable = true;
				} else if (lines[i].equals("pipe")) {
					program.piped = true;
				} else if (lines[i].equals("pooled")) {
					program.pooled = true;
				}
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public void save() {
		try {
			MQLCommand command = new MQLCommand();
			Context context = getContext();
			this.program.open(context);
			try {
				String modString = "";
				String programName = this.program.getName();
				boolean changedName = !programName.equals(getName());
				if (changedName) {
					modString = modString + " name \"" + getName() + "\"";
				}

				command.executeCommand(context, MessageFormat.format(MQL_INFO, new Object[] { this.program.getName() }));
				String[] info = command.getResult().trim().split("\\|");
				if (!info[0].equals(getDescription())) {
					modString = modString + " description \"" + getDescription() + "\"";
				}
				boolean oldIsHidden = info[1].equalsIgnoreCase("true");
				if (oldIsHidden != isHidden()) {
					modString = modString + (isHidden() ? " hidden" : " nothidden");
				}
				String oldProgramType = this.program.isMqlProgram() ? "mql" : this.program.isJavaProgram() ? "java" : "external";
				if (!oldProgramType.equals(this.programType)) {
					modString = modString + " " + this.programType;
				}
				String oldCode = this.program.getCode(context);
				if (!oldCode.equals(this.code)) {
					String codeToStore = this.code.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'");
					modString = modString + " code '" + codeToStore + "'";
				}
				boolean oldIsImmediate = info[2].equals("immediate");
				if (oldIsImmediate != isImmediate()) {
					modString = modString + (isHidden() ? " execute immediate" : " execute deferred");
				}

				EnoTreeProgram oldProg = new EnoTreeProgram(getName());
				fillAdditional(oldProg);
				if (oldProg.isNeedsBusinessObject() != isNeedsBusinessObject()) {
					modString = modString + (isNeedsBusinessObject() ? " " : " !") + "needsbusinessobject";
				}
				if (oldProg.isDownloadable() != isDownloadable()) {
					modString = modString + (isDownloadable() ? " " : " !") + "downloadable";
				}
				if (oldProg.isPooled() != isPooled()) {
					modString = modString + (isPooled() ? " " : " !") + "pooled";
				}
				if (oldProg.isPiped() != isPiped()) {
					modString = modString + (isPiped() ? " " : " !") + "pipe";
				}
				EnoTreePerson oldUser = info.length > 3 ? new EnoTreePerson(info[3]) : null;
				if ((this.user == null) && (oldUser != null)) {
					modString = modString + " execute user \"\"";
				} else if ((this.user != null) && (!this.user.equals(oldUser))) {
					modString = modString + " execute user \"" + this.user.getName() + "\"";
				}

				if (!modString.equals("")) {
					command.executeCommand(context, "escape modify program \"" + programName + "\" " + modString + ";");
				}

				allPrograms = null;
				if (changedName) {
					for (EnoTreeProgram p : getAllPrograms(false)) {
						if (this.name.equals(p.getName())) {
							this.program = p.program;
							break;
						}
					}

				}

				refresh();
			} finally {
				this.program.close(context);
			}
		} catch (Exception ex) {
			EnoEclipseLogger.getLogger().severe(ex.getMessage());
		}
	}

	public EnoTreeBusiness[] getChildren(boolean forceUpdate) throws EnoEclipseException, MatrixException {
		if (forceUpdate) {
			this.children = null;
		}
		if (this.children == null) {
			this.children = new ArrayList<EnoTreeBusiness>();
		}

		return (EnoTreeBusiness[])this.children.toArray(new EnoTreeBusiness[this.children.size()]);
	}

	public static void saveJpoToFile(String jpoDirectory, String programName) throws EnoEclipseException, MatrixException, IOException {
		for (EnoTreeProgram p : getAllPrograms(false)) {
			if (programName.equals(p.getName())) {
				saveJpoToFile(jpoDirectory, p.program, null);
				break;
			}
		}
	}

	protected static void saveJpoToFile(String jpoDirectory, Program program, ArrayList<String> alAlreadyDone) throws EnoEclipseException, MatrixException, IOException {
		Context context = getContext();
		String name = program.getName();
		String classCode = null;
		String extension = "";

		program.open(context);
		try {
			classCode = program.getCode(context);

			if (program.isJavaProgram()) {
				String jpoName = name;
				String packageName = "";
				String className = jpoName + JPO_SUFFIX;
				if (jpoName.contains(".")) {
					packageName = jpoName.substring(0, jpoName.lastIndexOf("."));
					className = jpoName.substring(jpoName.lastIndexOf(".") + 1) + JPO_SUFFIX;
				}
				classCode = convertFromMatrix(jpoDirectory, packageName, className, classCode, alAlreadyDone);

				extension = "java";
				name = className;

				String[] packageFolders = packageName.split("\\.");
				for (String packageFolder : packageFolders) {
					jpoDirectory = jpoDirectory + File.separator + packageFolder;
					File otherProgFolder = new File(jpoDirectory);
					if (!otherProgFolder.exists()) {
						otherProgFolder.mkdir();
					}
				}
			} else if (program.isMqlProgram()) {
				extension = "tcl";
			}
		} finally {
			program.close(context);
		}

		FileWriter writer = new FileWriter(jpoDirectory + File.separator + name + "." + extension, false);
		try {
			writer.write(classCode);
		} finally {
			writer.close();
		}
	}

	private static String convertFromMatrix(String jpoDirectory, String packageName, String className, String matrixCode, ArrayList<String> alAlreadyDone) throws MatrixException, EnoEclipseException, IOException {
		String retVal = matrixCode;

		if ((packageName != null) && (!packageName.equals(""))) {
			retVal = "package " + packageName + ";\n" + retVal;
		}

		retVal = retVal.replaceAll("\\$\\{CLASSNAME\\}", className);
		int start = retVal.indexOf("${");
		while (start >= 0) {
			int stop = retVal.indexOf("}", start);
			String innerName = retVal.substring(start + 2, stop);
			int indexSemi = innerName.indexOf(":");
			String operator = "";
			if (indexSemi > 0) {
				operator = innerName.substring(0, indexSemi).trim();
			}
			String newString = "";
			if (operator.toUpperCase().equals("CLASS")) {
				String innerClassName = innerName.substring(indexSemi + 1).trim();
				if (alAlreadyDone == null) {
					alAlreadyDone = new ArrayList<String>();
				}
				if ((jpoDirectory != null) && (alAlreadyDone != null) && (!alAlreadyDone.contains(innerClassName))) {
					alAlreadyDone.add(innerClassName);
					boolean bFound = false;
					for (EnoTreeProgram innerProgram : getAllPrograms(false)) {
						if ((innerClassName.equals(innerProgram.getName())) || (innerClassName.equals("matrix.jpo." + innerProgram.getName()))) {
							saveJpoToFile(jpoDirectory, innerProgram.program, alAlreadyDone);
							bFound = true;
							break;
						}
					}
					if (!bFound) {
						throw new EnoEclipseException("Inner JPO: " + innerClassName + " was not found!");
					}
				}

				newString = innerClassName + JPO_SUFFIX;
				retVal = retVal.replaceFirst("\\$\\{" + innerName + "\\}", newString);

				start = retVal.indexOf("${", start + newString.length() + 1);
			} else {
				start = retVal.indexOf("${", stop + 1);
			}

		}

		//Commented to avoid removing backslash.
		//retVal = retVal.replaceAll("\\\\\\\\", "\\\\");

		return retVal;
	}

	public static String convertToJavaForm(EnoTreeProgram program, String code) throws MatrixException, EnoEclipseException, IOException {
		String jpoName = program.getName();
		String packageName = "";
		String className = jpoName + JPO_SUFFIX;
		if (jpoName.contains(".")) {
			packageName = jpoName.substring(0, jpoName.lastIndexOf("."));
			className = jpoName.substring(jpoName.lastIndexOf(".") + 1) + JPO_SUFFIX;
		}
		String retVal = convertFromMatrix(null, packageName, className, code, null);
		return retVal;
	}

	public static String returnOrRemovePackage(String code, boolean returnPackageWithoutRemoving) {
		String retVal = code;
		String packageName = "";

		int startPackage = retVal.indexOf("package ");
		while (startPackage >= 0) {
			if ((startPackage == 0) || (!Character.isJavaIdentifierPart(retVal.charAt(startPackage - 1)))) {
				int startComment = startPackage;
				boolean closed = false;
				boolean newLine = false;
				boolean bComment = false;
				while ((startComment >= 0) && ((!closed) || (!newLine))) {
					if (retVal.substring(startComment, startComment + 2).equals("*/")) {
						closed = true;
					} else if (retVal.substring(startComment, startComment + 1).equals("\n")) {
						newLine = true;
					} else if (retVal.substring(startComment, startComment + 2).equals("//")) {
						if (!newLine) {
							bComment = true;
							break;
						}
					} else if ((retVal.substring(startComment, startComment + 2).equals("/*")) && (!closed)) {
						bComment = true;
						break;
					}

					startComment--;
				}

				if (!bComment) {
					int endPackage = retVal.indexOf(";", startPackage);
					packageName = retVal.substring(startPackage + "package ".length(), endPackage).trim();
					if (returnPackageWithoutRemoving) {
						return packageName;
					}
					do {
						if (endPackage >= retVal.length()) {
							break; 
						}
						endPackage++;
					} while ((Character.isWhitespace(retVal.charAt(endPackage))) && (retVal.charAt(endPackage) != '\n'));

					if (!Character.isWhitespace(retVal.charAt(endPackage))) {
						endPackage--;
					}
					retVal = retVal.substring(0, startPackage) + retVal.substring(endPackage + 1);
					startPackage = -1;
				} else {
					startPackage = retVal.indexOf("package ", startPackage + 1);
				}
			} else {
				startPackage = retVal.indexOf("package ", startPackage + 1);
			}
		}
		if (returnPackageWithoutRemoving) {
			return "";
		}
		return retVal;
	}

	public static String convertToMacroForm(EnoTreeProgram program, String code) throws EnoEclipseException, MatrixException, IOException {
		String retVal = code;

		retVal = returnOrRemovePackage(retVal, false);

		int startSuffix = retVal.indexOf(JPO_SUFFIX);
		while (startSuffix >= 0) {
			String identifier = "";
			int startIdentifier = startSuffix;
			do startIdentifier--; while ((Character.isJavaIdentifierPart(retVal.charAt(startIdentifier))) || (retVal.charAt(startIdentifier) == '.'));

			startIdentifier++;

			int previousWordStart = startIdentifier;
			do previousWordStart--; while (retVal.charAt(previousWordStart) == ' ');
			do { 
				if (previousWordStart <= 0) {
					break;
				} 
				previousWordStart--;
			} while (Character.isJavaIdentifierPart(retVal.charAt(previousWordStart)));

			if ((previousWordStart > 0) || (!retVal.startsWith("class"))) {
				previousWordStart++;
			}
			if (retVal.substring(previousWordStart, startIdentifier - 1).trim().equals("class")) {
				identifier = "${CLASSNAME}";
			} else {
				identifier = "${CLASS:" + retVal.substring(startIdentifier, startSuffix) + "}";
			}

			retVal = retVal.substring(0, startIdentifier) + identifier + retVal.substring(startSuffix + JPO_SUFFIX.length());
			startSuffix = retVal.indexOf(JPO_SUFFIX, startIdentifier + identifier.length());
		}
		return retVal;
	}

	public static String readJpoFromFileAndStoreToMatrix(String fullPath, String fileName, boolean store) throws EnoEclipseException, MatrixException, IOException {
		String jpoName = null;
		if (fileName.endsWith("java")) {
			String className = fileName.substring(0, fileName.indexOf("."));
			jpoName = className.substring(0, className.length() - JPO_SUFFIX.length());
		} else if (fileName.endsWith("mql")) {
			jpoName = fileName.substring(0, fileName.indexOf("."));
		} else {
			jpoName = fileName;
		}
		BufferedReader reader = new BufferedReader(new FileReader(fullPath));
		String code;
		try {
			String line = null;
			code = "";
			while ((line = reader.readLine()) != null) {
				code = code + line + "\n";
			}
		} finally {
			reader.close();
		}

		String packageName = "";
		if (fileName.endsWith("java")) {
			packageName = returnOrRemovePackage(code, true);
			if (!packageName.equals("")) {
				jpoName = packageName + "." + jpoName;
			}
			code = convertToMacroForm(new EnoTreeProgram(jpoName), code);
		}

		if (store) {
			EnoTreeProgram programToUpdate = new EnoTreeProgram(jpoName);
			programToUpdate.fillBasics();
			programToUpdate.setCode(code);
			programToUpdate.save();
		}
		return packageName;
	}

	public static void clearCache() {
		allPrograms = null;
	}
}