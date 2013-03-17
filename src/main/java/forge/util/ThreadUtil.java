/*
 * The files in the directory "net/slightlymagic/braids" and in all subdirectories of it (the "Files") are
 * Copyright 2011 Braids Cabal-Conjurer. They are available under either Forge's
 * main license (the GNU Public License; see LICENSE.txt in Forge's top directory)
 * or under the Apache License, as explained below.
 *
 * The Files are additionally licensed under the Apache License, Version 2.0 (the
 * "Apache License"); you may not use the files in this directory except in
 * compliance with one of its two licenses.  You may obtain a copy of the Apache
 * License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Apache License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License for the specific language governing permissions and
 * limitations under the Apache License.
 *
 */
package forge.util;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

/**
 * Some general-purpose functions.
 */
public final class ThreadUtil {

    /**
     * Invoke the given Runnable in an Event Dispatch Thread and wait for it to
     * finish; but <B>try to use SwingUtilities.invokeLater instead whenever
     * feasible.</B>
     * 
     * Exceptions generated by SwingUtilities.invokeAndWait (if used), are
     * rethrown as RuntimeExceptions.
     * 
     * @param proc
     *            the Runnable to run
     * @see javax.swing.SwingUtilities#invokeLater(Runnable)
     */
    public static void invokeInEventDispatchThreadAndWait(final Runnable proc) {
        // by
        // Braids
        // on
        // 8/18/11
        // 11:19
        // PM
        if (SwingUtilities.isEventDispatchThread()) {
            // Just run in the current thread.
            proc.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(proc);
            } catch (final InterruptedException exn) {
                throw new RuntimeException(exn);
                // 11:19 PM
            } catch (final InvocationTargetException exn) {
                throw new RuntimeException(exn);
                // 11:19 PM
            }
        }
    }


}
