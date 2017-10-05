package ua.napps.scorekeeper.utils;

import android.support.v7.widget.DefaultItemAnimator;

// Don't flash items when changing content
public class NoChangeAnimator extends DefaultItemAnimator {
  public NoChangeAnimator() {
    setSupportsChangeAnimations(false);
  }
}