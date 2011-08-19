package net.sparktank.morrigan.model.media.internal.db.mmdb;

import com.vaguehope.morrigan.model.media.ILocalMixedMediaDb;
import com.vaguehope.morrigan.model.media.IMixedMediaItem;
import com.vaguehope.morrigan.model.media.IMixedMediaStorageLayer;
import com.vaguehope.sqlitewrapper.DbException;

import net.sparktank.morrigan.model.media.internal.db.MediaItemDbConfig;

public class LocalMixedMediaDb extends AbstractMixedMediaDb<ILocalMixedMediaDb> implements ILocalMixedMediaDb {
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	protected LocalMixedMediaDb (String listName, MediaItemDbConfig config, IMixedMediaStorageLayer<IMixedMediaItem> dbLayer) {
		super(listName, config, dbLayer);
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
	@Override
	public String getType () {
		return TYPE;
	}
	
	@Override
	public ILocalMixedMediaDb getTransactionalClone () throws DbException {
		ILocalMixedMediaDb r = LocalMixedMediaDbFactory.getTransactional(getDbPath());
		return r;
	}
	
//	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
}
