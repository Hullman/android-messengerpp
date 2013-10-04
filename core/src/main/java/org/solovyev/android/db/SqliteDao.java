package org.solovyev.android.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

import static com.google.common.collect.Iterables.getFirst;
import static org.solovyev.android.db.AndroidDbUtils.doDbExec;
import static org.solovyev.android.db.AndroidDbUtils.doDbExecs;
import static org.solovyev.android.db.AndroidDbUtils.doDbQuery;

public final class SqliteDao<E> extends AbstractSQLiteHelper implements Dao<E> {

	@Nonnull
	private final String tableName;

	@Nonnull
	private final String idColumnName;

	@Nonnull
	private final SqliteDaoEntityMapper<E> mapper;

	public SqliteDao(@Nonnull String tableName,
					 @Nonnull String idColumnName,
					 @Nonnull SqliteDaoEntityMapper<E> mapper,
					 @Nonnull Context context,
					 @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
		super(context, sqliteOpenHelper);
		this.tableName = tableName;
		this.idColumnName = idColumnName;
		this.mapper = mapper;
	}

	@Override
	public long create(@Nonnull E entity) {
		return doDbExec(getSqliteOpenHelper(), new InsertEntity(entity));
	}

	@Nullable
	@Override
	public E read(@Nonnull String id) {
		final Collection<E> accounts = doDbQuery(getSqliteOpenHelper(), new LoadEntity(getContext(), id, getSqliteOpenHelper()));
		return getFirst(accounts, null);
	}

	@Nonnull
	@Override
	public Collection<E> readAll() {
		return doDbQuery(getSqliteOpenHelper(), new LoadEntity(getContext(), null, getSqliteOpenHelper()));
	}

	@Override
	public long update(@Nonnull E entity) {
		return doDbExec(getSqliteOpenHelper(), new UpdateEntity(entity));
	}

	@Override
	public void delete(@Nonnull E entity) {
		deleteById(mapper.getId(entity));
	}

	@Override
	public void deleteById(@Nonnull String id) {
		doDbExec(getSqliteOpenHelper(), new DeleteEntity(id));
	}

	@Override
	public void deleteAll() {
		doDbExecs(getSqliteOpenHelper(), Arrays.<DbExec>asList(DeleteAllRowsDbExec.newInstance(tableName)));
	}

	/*
	**********************************************************************
	*
	*                           STATIC
	*
	**********************************************************************
	*/

	private class InsertEntity extends AbstractObjectDbExec<E> {

		public InsertEntity(@Nonnull E entity) {
			super(entity);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final E entity = getNotNullObject();

			final ContentValues values = mapper.toContentValues(entity);

			return db.insert(tableName, null, values);
		}
	}

	private class UpdateEntity extends AbstractObjectDbExec<E> {

		public UpdateEntity(@Nonnull E entity) {
			super(entity);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final E entity = getNotNullObject();

			final ContentValues values = mapper.toContentValues(entity);

			return db.update(tableName, values, whereIdEqualsTo(), new String[]{mapper.getId(entity)});
		}
	}

	private class LoadEntity extends AbstractDbQuery<Collection<E>> {

		@Nullable
		private final String id;

		protected LoadEntity(@Nonnull Context context,
							 @Nullable String id,
							 @Nonnull SQLiteOpenHelper sqliteOpenHelper) {
			super(context, sqliteOpenHelper);
			this.id = id;
		}

		@Nonnull
		@Override
		public Cursor createCursor(@Nonnull SQLiteDatabase db) {
			if (id != null) {
				return db.query(tableName, null, whereIdEqualsTo(), new String[]{id}, null, null, null);
			} else {
				return db.query(tableName, null, null, null, null, null, null);
			}
		}

		@Nonnull
		@Override
		public Collection<E> retrieveData(@Nonnull Cursor cursor) {
			return new ListMapper<E>(mapper.getCursorMapper()).convert(cursor);
		}
	}


	private class DeleteEntity extends AbstractObjectDbExec<String> {

		public DeleteEntity(@Nonnull String id) {
			super(id);
		}

		@Override
		public long exec(@Nonnull SQLiteDatabase db) {
			final String id = getNotNullObject();
			return db.delete(tableName, whereIdEqualsTo(), new String[]{id});
		}
	}

	@Nonnull
	private String whereIdEqualsTo() {
		return idColumnName + " = ?";
	}

}
