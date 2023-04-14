package com.voitov.ownobservableimpl;

import android.app.Application;

public class MyApplication extends Application {
    private final ApplicationCompositionRule compositionRoot = new ApplicationCompositionRule();

    public ApplicationCompositionRule getCompositionRoot() {
        return compositionRoot;
    }
}
