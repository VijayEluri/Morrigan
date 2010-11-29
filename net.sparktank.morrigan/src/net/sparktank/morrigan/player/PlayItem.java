package net.sparktank.morrigan.player;

import net.sparktank.morrigan.model.helper.EqualHelper;
import net.sparktank.morrigan.model.media.interfaces.IMediaTrack;
import net.sparktank.morrigan.model.media.interfaces.IMediaTrackList;

public class PlayItem {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public IMediaTrackList<? extends IMediaTrack> list;
	public IMediaTrack item;
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	public PlayItem (IMediaTrackList<? extends IMediaTrack> list, IMediaTrack item) {
		this.list = list;
		this.item = item;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public String toString() {
		if (this.item == null) {
			return this.list.getListName();
		}
		
		return this.list.getListName() + "/" + this.item.getTitle();
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public boolean equals (Object obj) {
		if ( obj == null ) return false;
		if ( this == obj ) return true;
		if ( !(obj instanceof PlayItem) ) return false;
		PlayItem that = (PlayItem)obj;
		
		return EqualHelper.areEqual(this.list, that.list)
			&& EqualHelper.areEqual(this.item, that.item);
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
	    hash = hash * 31 + (this.list == null ? 0 : this.list.hashCode());
	    hash = hash * 31 + (this.item == null ? 0 : this.item.hashCode());
	    return hash;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
