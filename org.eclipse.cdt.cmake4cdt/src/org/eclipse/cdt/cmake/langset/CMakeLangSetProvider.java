/*******************************************************************************
 * Copyright (c) 2014, 2015 Rohde & Schwarz GmbH & Co. KG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Runge - initial implementation of cmake support
 *******************************************************************************/
package org.eclipse.cdt.cmake.langset;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.cmake.CMakeOutputPath;
import org.eclipse.cdt.cmake.langset.IBuildCommandParserEx.CompileUnitInfo;
import org.eclipse.cdt.core.language.settings.providers.ILanguageSettingsProvider;
import org.eclipse.cdt.core.language.settings.providers.LanguageSettingsBaseProvider;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.managedbuilder.language.settings.providers.AbstractBuildCommandParser.ResourceScope;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;



public class CMakeLangSetProvider extends LanguageSettingsBaseProvider
		implements ILanguageSettingsProvider {

	static final String COMPILE_CMDS_FILENAME = "compile_commands.json"; 
	
	public CMakeLangSetProvider() {
	}


	public CMakeLangSetProvider(String id, String name) {
		super(id, name);
		// TODO Auto-generated constructor stub
	}

	public CMakeLangSetProvider(String id, String name, List<String> languages,
			List<ICLanguageSettingEntry> entries) {
		super(id, name, languages, entries);
		// TODO Auto-generated constructor stub
	}

	public CMakeLangSetProvider(String id, String name, List<String> languages,
			List<ICLanguageSettingEntry> entries, Map<String, String> properties) {
		super(id, name, languages, entries, properties);
		// TODO Auto-generated constructor stub
	}

	public List<ICLanguageSettingEntry> getSettingEntries(ICConfigurationDescription cfgDescription, IResource rc, String languageId) {
		
//		PerProjectSettings projSettings = null;
		if(cfgDescription == null || rc == null) {
			return null;
		}
		
		IProject project = rc.getProject();
		IProjectDescription prjDesc;
		try {
			IBuildCommandParserEx commandParser = null;
			CMakeCompileCmdsCwdTracker cwdTracker = new CMakeCompileCmdsCwdTracker();
			IPath outputPath = CMakeOutputPath.getPath(project, cfgDescription.getName());
			String filename = outputPath.append(COMPILE_CMDS_FILENAME).toString();
			
			// try to figure out which compiler was used. Assume gcc for now
			CompileCmdsHandler cmdHdl = new CompileCmdsHandler(filename);
			if(cmdHdl.hasChanged()) {
				cmdHdl.parseCMakeCompileCommands();
			
				// 		m_commandParser = new GCCBuildCommandParser();
				if(cmdHdl.hasSourcesOutsideProject()) {
					Job job = new AddForeignSourcesWorkspaceJob("Creating \"Foreign Sources\" folders", project, cmdHdl.getSourcesOutsideProject()); 
					job.schedule();
				}
			}

			// cmdHdl.detectCompiler();
			commandParser = new CMakeCompileCommandParserGCC();
			commandParser.startup(cfgDescription, cwdTracker);
			// commandParser.setResourceScope(ResourceScope.PROJECT);
			
			for(CompileUnitInfo cu: cmdHdl.getSources()) {
				commandParser.processLine(cu.getCmdLine());
				
			}
			
			commandParser.shutdown();
		
			List<ICLanguageSettingEntry> entries = new ArrayList<ICLanguageSettingEntry>();

			// List<ICLanguageSettingEntry> entries = commandParser.getSettingEntries(cfgDescription, rc, languageId);
			return entries;
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	class AddForeignSourcesWorkspaceJob extends WorkspaceJob {

		private List<CompileUnitInfo> cuInfoList = null;
		private IProject project = null;

		/**
		 * @param name
		 */
		public AddForeignSourcesWorkspaceJob(String name, IProject project, List<CompileUnitInfo> cuInfos) {
			super(name);
			this.cuInfoList = cuInfos;
			this.project = project;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.resources.WorkspaceJob#runInWorkspace(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor)	throws CoreException {
			try {
				IFolder fsFolder = project.getFolder(new Path("ForeignSources"));
				if(!fsFolder.exists()) {
					fsFolder.create(IResource.VIRTUAL, true, null);
				}
				for(CompileUnitInfo cuInfo: cuInfoList) {
					File fsFileName = new File(cuInfo.getParsedResourceName());
					IFile fsFile = fsFolder.getFile(fsFileName.getName());
					IPath linkTarget = new Path(cuInfo.getParsedResourceName());
					fsFile.createLink(linkTarget, IResource.REPLACE, null);
				}
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return Status.OK_STATUS;
		}
	}

}
