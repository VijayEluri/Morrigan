/*
 * Copyright 2010 Fae Hutter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package net.sparktank.morrigan.android.tasks;

import java.io.IOException;
import java.net.ConnectException;

import net.sparktank.morrigan.android.Constants;
import net.sparktank.morrigan.android.helper.HttpHelper;
import net.sparktank.morrigan.android.model.MlistStateList;
import net.sparktank.morrigan.android.model.MlistStateListChangeListener;
import net.sparktank.morrigan.android.model.ServerReference;
import net.sparktank.morrigan.android.model.impl.MlistStateListImpl;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

public class GetMlistsTask extends AsyncTask<Void, Void, MlistStateList> {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private final Activity activity;
	private final ServerReference serverReference;
	private final MlistStateListChangeListener changedListener;
	
	private Exception exception;
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public GetMlistsTask (Activity activity, ServerReference serverReference, MlistStateListChangeListener changedListener) {
		this.activity = activity;
		this.serverReference = serverReference;
		this.changedListener = changedListener;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	// In UI thread:
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.activity.setProgressBarIndeterminateVisibility(true);
	}
	
	// In background thread:
	@Override
	protected MlistStateList doInBackground(Void... params) {
		String url = this.serverReference.getBaseUrl();
		url = url.concat(Constants.CONTEXT_MLISTS);
		
		try {
			String resp = HttpHelper.getUrlContent(url);
			MlistStateList state = new MlistStateListImpl(resp, this.serverReference);
			return state;
		}
		catch (ConnectException e) {
			this.exception = e;
			return null;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}
	
	// In UI thread:
	@Override
	protected void onPostExecute(MlistStateList result) {
		super.onPostExecute(result);
		
		if (this.exception != null) { // TODO handle this better.
			Toast.makeText(this.activity, this.exception.getMessage(), Toast.LENGTH_LONG).show();
		}
		
		if (this.changedListener != null) this.changedListener.onMlistsChange(result);
		
		this.activity.setProgressBarIndeterminateVisibility(false);
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
