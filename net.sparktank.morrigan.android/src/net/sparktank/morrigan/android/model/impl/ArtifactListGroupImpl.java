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

package net.sparktank.morrigan.android.model.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sparktank.morrigan.android.model.Artifact;
import net.sparktank.morrigan.android.model.ArtifactList;

public class ArtifactListGroupImpl implements ArtifactList {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	Map<String, ArtifactList> artifactLists = new HashMap<String, ArtifactList>();
	List<? extends Artifact> cache = null;
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public List<? extends Artifact> getArtifactList() {
		return this.cache;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public void addList (String key, ArtifactList list) {
		this.artifactLists.put(key, list);
		updateCache();
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	private void updateCache () {
		List<Artifact> list = new LinkedList<Artifact>();
		
		Collection<ArtifactList> lists = this.artifactLists.values();
		List<ArtifactList> sortedList = new LinkedList<ArtifactList>(lists);
		Collections.sort(sortedList);
		
		for (ArtifactList l : sortedList) {
			list.addAll(l.getArtifactList());
		}
		
		this.cache = list;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public String getSortKey() {
		return "0";
	}
	
	@Override
	public int compareTo(ArtifactList another) {
		return 0; // There is no data to sort on.
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
