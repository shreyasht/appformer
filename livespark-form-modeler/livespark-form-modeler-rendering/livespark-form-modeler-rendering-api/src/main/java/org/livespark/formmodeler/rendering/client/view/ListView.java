package org.livespark.formmodeler.rendering.client.view;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.common.client.api.RemoteCallback;
import org.jboss.errai.ioc.client.container.IOCBeanDef;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.widget.ListWidget;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.livespark.formmodeler.rendering.client.shared.FormModel;

import com.github.gwtbootstrap.client.ui.Button;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Composite;

public abstract class ListView<M extends FormModel, W extends ListItemView<M>> extends Composite {

    @Inject
    @DataField
    protected Button create;

    @Inject
    @DataField
    protected ListWidget<M, W> items;

    @Inject
    protected SyncBeanManager beanManager;

    protected final DeleteExecutor<M> deleteCommand = new DeleteExecutor<M>() {

        @Override
        public void execute( M model ) {
            delete(model);
        }
    };

    @PostConstruct
    protected void init() {
        create.setText( "Create" );
        loadData( new RemoteCallback<List<M>>() {

            @Override
            public void callback( List<M> response ) {
                items.setItems( response );
            }
        } );
    }

    protected abstract void loadData( RemoteCallback<List<M>> callback );

    protected abstract void remoteDelete( M model, RemoteCallback<Boolean> callback );

    protected FormView<M> getForm() {
        IOCBeanDef<? extends FormView<M>> beanDef = beanManager.lookupBean( getFormType() );

        return beanDef.getInstance();
    }

    protected abstract Class<? extends FormView<M>> getFormType();

    protected abstract String getFormTitle();

    protected abstract String getFormId();

    public void delete( final M model ) {
        remoteDelete( model,
                      new RemoteCallback<Boolean>() {

                          @Override
                          public void callback( Boolean response ) {
                              if ( response ) {
                                  items.getValue().remove( model );
                              }
                          }
                      } );
    }

    @EventHandler( "create" )
    public void onCreateClick( ClickEvent event ) {
        FormView<M> form = getForm();
        form.setCallback( new RemoteCallback<M>() {
            @Override
            public void callback( M response ) {
                items.getValue().add( response );
            }
        } );

        new ModalForm( form, getFormTitle(), getFormId() ).show();
    }

    public interface DeleteExecutor<T> {
        public void execute(T model);
    }

}