/*******************************************************************************
 * Copyright (C) 2011 Atlas of Living Australia
 * All Rights Reserved.
 * 
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.delta.editor.directives;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Before;

import au.org.ala.delta.editor.slotfile.Directive;
import au.org.ala.delta.editor.slotfile.DirectiveInstance;
import au.org.ala.delta.editor.slotfile.directive.DirectiveInOutState;
import au.org.ala.delta.model.DefaultDataSetFactory;
import au.org.ala.delta.model.MutableDeltaDataSet;
import au.org.ala.delta.translation.PrintFile;
import junit.framework.TestCase;

/**
 * Helps with testing DirOut functors.
 */
public abstract class DirOutTest extends TestCase {

	protected MutableDeltaDataSet _dataSet;
	protected DirectiveInOutState _state;
	private ByteArrayOutputStream _bytesOut;

	public DirOutTest() {
		super();
	}

	public DirOutTest(String name) {
		super(name);
	}

	@Before
	public void setUp() throws Exception {
		_dataSet = new DefaultDataSetFactory().createDataSet("test");
		
		_bytesOut = new ByteArrayOutputStream();
		PrintStream out = new PrintStream(_bytesOut, true, "utf-8");	
		PrintFile _printer = new PrintFile(out, 80);
		_state = new DirectiveInOutState(_dataSet);
		_state.setPrinter(_printer);
		
		DirectiveInstance directive = new DirectiveInstance(getDirective(), null); 
		_state.setCurrentDirective(directive);
	}
	
	protected abstract Directive getDirective(); 

	protected String output() throws Exception {
		_bytesOut.flush();
		String output = new String(_bytesOut.toByteArray(), "utf-8");
		return output.replace("\r\n", "\n");
	}

}
