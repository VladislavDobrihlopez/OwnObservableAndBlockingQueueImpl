package com.voitov.ownobservableimpl;

public abstract class ScreenState {
    public static class ComputationCompleted extends ScreenState {
        private final int elapsedTime;
        private final int consumedMessages;

        public ComputationCompleted(int elapsedTime, int consumedMessages) {
            this.elapsedTime = elapsedTime;
            this.consumedMessages = consumedMessages;
        }

        public int getElapsedTime() {
            return elapsedTime;
        }

        public int getConsumedMessages() {
            return consumedMessages;
        }
    }

    public static class ComputationCancelled extends ScreenState {
    }
}
