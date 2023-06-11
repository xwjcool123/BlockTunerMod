/*
 *     Copyright (c) 2023, xwjcool.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cool.xwj.blocktuner;

public class NoteNames {
    public static String get(int note) {
        note %= 12;
        return get(note, BlockTunerConfig.getKeySignature());
    }

    public static String get(int note, int keySignature) {
        String noteName = "";
        switch (note) {
            case 0 -> {
                if (keySignature >= -1) {
                    noteName = "F\u266f";
                } else if (keySignature <= -3) {
                    noteName = "G\u266d";
                } else {
                    noteName = "F\u266f | G\u266d";
                }
            }
            case 1 -> {
                if (keySignature >= 6) {
                    noteName = "F\ud834\udd2a";
                } else if (keySignature <= 4 && keySignature >= -6) {
                    noteName = "G";
                    if (keySignature >= 3 || keySignature <= -5) {
                        noteName = "G\u266e";
                    }
                } else if (keySignature == 5){
                    noteName = "F\ud834\udd2a | G\u266e";
                } else {
                    noteName = "G\u266e | A\ud834\udd2b";
                }
            }
            case 2 -> {
                if (keySignature >= 1) {
                    noteName = "G\u266f";
                } else if (keySignature <= -1) {
                    noteName = "A\u266d";
                } else {
                    noteName = "G\u266f | A\u266d";
                }
            }
            case 3 -> {
                if (keySignature >= 7) {
                    noteName = "G\ud834\udd2a | A\u266e";
                } else if (keySignature >= -4) {
                    noteName = "A";
                    if (keySignature <= -3 || keySignature >= 5) {
                        noteName = "A\u266e";
                    }
                } else if (keySignature <= -6) {
                    noteName = "B\ud834\udd2b";
                } else {
                    noteName = "A\u266e | B\ud834\udd2b";
                }
            }
            case 4 -> {
                if (keySignature >= 3) {
                    noteName = "A\u266f";
                } else if (keySignature <= 1) {
                    noteName = "B\u266d";
                } else {
                    noteName = "A\u266f | B\u266d";
                }
            }
            case 5 -> {
                if (keySignature >= -2) {
                    noteName = "B";
                    if (keySignature <= -1 || keySignature >= 7) {
                        noteName = "B\u266e";
                    }
                } else if (keySignature <= -4) {
                    noteName = "C\u266d";
                } else {
                    noteName = "B\u266e | C\u266d";
                }
            }
            case 6 -> {
                if (keySignature >= 5) {
                    noteName = "B\u266f";
                } else if (keySignature <= 3) {
                    noteName = "C";
                    if (keySignature >= 2 || keySignature <= -6) {
                        noteName = "C\u266e";
                    }
                } else {
                    noteName = "B\u266f | C\u266e";
                }
            }
            case 7 -> {
                if (keySignature >= 0) {
                    noteName = "C\u266f";
                } else if (keySignature <= -2) {
                    noteName = "D\u266d";
                } else {
                    noteName = "C\u266f | D\u266d";
                }
            }
            case 8 -> {
                if (keySignature >= 7) {
                    noteName = "C\ud834\udd2a";
                } else if (keySignature >= -5 && keySignature <= 5) {
                    noteName = "D";
                    if (keySignature <= -4 || keySignature >= 4) {
                        noteName = "D\u266e";
                    }
                } else if (keySignature <= -7) {
                    noteName = "E\ud834\udd2b";
                } else if (keySignature == -6) {
                    noteName = "C\ud834\udd2a | D\u266e";
                } else {
                    noteName = "D\u266e | E\ud834\udd2b";
                }
            }
            case 9 -> {
                if (keySignature >= 2) {
                    noteName = "D\u266f";
                } else if (keySignature <= 0) {
                    noteName = "E\u266d";
                } else {
                    noteName = "D\u266f | E\u266d";
                }
            }
            case 10 -> {
                if (keySignature >= -3) {
                    noteName = "E";
                    if (keySignature <= -2 || keySignature >= 6) {
                        noteName = "E\u266e";
                    }
                } else if (keySignature <= -5) {
                    noteName = "F\u266d";
                } else {
                    noteName = "E\u266e | F\u266d";
                }
            }
            case 11 -> {
                if (keySignature >= 4) {
                    noteName = "E\u266f";
                } else if (keySignature <= 2) {
                    noteName = "F";
                    if (keySignature >= 1 || keySignature <= -7) {
                        noteName = "F\u266e";
                    }
                } else {
                    noteName = "E\u266f | F\u266e";
                }
            }
        }
        return noteName;
    }
}
