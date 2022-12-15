private static void copyAssetFiles(InputStream in, OutputStream out) {
    try {

        byte[] buffer = new byte[BUFFER_SIZE];
        int read;

        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }

        in.close();
        in = null;
        out.flush();
        out.close();
        out = null;

    } catch (IOException e) {
        e.printStackTrace();
    } catch (NullPointerException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }
}


private void loadDictionaryFromAsserts(){

    try{
        AssetManager assetFiles = BobbleApp.getInstance().getContext().getAssets();
        String[] files = assetFiles.list("PrimaryNgramDictionary.en.dict");
        // Initialize streams
        InputStream in = null;
        OutputStream out = null;
        for (int i = 0; i < 2; i++) {
            in = assetFiles.open("PrimaryNgramDictionary.en.dict/" + files[i]);

           // File myFilesDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/data/data/com.touchtalent.bobbleapp/files/PrimaryNgramDictionary.en.dict");
            File myFilesDir = new File(mDictFile.getAbsolutePath());
            if (!myFilesDir.exists()) {
                myFilesDir.mkdirs();
            }

            out = new FileOutputStream(
                    myFilesDir
                            + files[i]);

            copyAssetFiles(in, out);
        }

    }catch (FileNotFoundException e) {
        e.printStackTrace();
    } catch (NullPointerException e) {
        e.printStackTrace();
    } catch (Exception e) {
        e.printStackTrace();
    }

}

/**
 * Reloads the dictionary. Access is controlled on a per dictionary file basis.
 */
private final void asyncReloadDictionary() {
    if (mIsReloading.compareAndSet(false, true)) {
        asyncExecuteTaskWithWriteLock(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!mDictFile.exists() || mNeedsToRecreate) {
                        // If the dictionary file does not exist or contents have been updated,
                        // generate a new one.
                        loadDictionaryFromAsserts();
                        //createNewDictionaryLocked();
                    } else if (mBinaryDictionary == null) {
                        // Otherwise, load the existing dictionary.
                        loadBinaryDictionaryLocked();
                        if (mBinaryDictionary != null && !(isValidDictionaryLocked()
                                // TODO: remove the check below
                                && matchesExpectedBinaryDictFormatVersionForThisType(
                                mBinaryDictionary.getFormatVersion()))) {
                            // Binary dictionary or its format version is not valid. Regenerate
                            // the dictionary file. createNewDictionaryLocked will remove the
                            // existing files if appropriate.
                            createNewDictionaryLocked();
                        }
                    }
                    mNeedsToRecreate = false;
                } finally {
                    mIsReloading.set(false);
                }
            }
        });
    }
}