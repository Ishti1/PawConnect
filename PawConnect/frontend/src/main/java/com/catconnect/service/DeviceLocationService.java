package com.catconnect.service;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class DeviceLocationService {

    private DeviceLocationService() {
    }


    public static void locateAsync(
            Consumer<LocationResult> callback
    ) {

        Thread thread = new Thread(() -> {

            String os =
                    System.getProperty("os.name")
                            .toLowerCase();


            if (!os.contains("win")) {

                callback.accept(
                        LocationResult.failure(
                                "Automatic device location is currently supported on Windows."
                        )
                );

                return;
            }


            try {

                /*
                 * IMPORTANT:
                 *
                 * We output latitude and longitude using very
                 * specific prefixes:
                 *
                 * PAW_LAT=
                 * PAW_LON=
                 *
                 * This prevents PowerShell warnings/errors from
                 * accidentally being interpreted as coordinates.
                 */
                String script = """

                        Add-Type -AssemblyName System.Device

                        $watcher = New-Object System.Device.Location.GeoCoordinateWatcher(
                            [System.Device.Location.GeoPositionAccuracy]::High
                        )

                        $watcher.MovementThreshold = 1

                        $watcher.Start()

                        $deadline = (Get-Date).AddSeconds(10)

                        while (
                            $watcher.Position.Location.IsUnknown -and
                            (Get-Date) -lt $deadline
                        ) {
                            Start-Sleep -Milliseconds 300
                        }

                        if ($watcher.Position.Location.IsUnknown) {

                            Write-Output "PAW_NO_LOCATION"

                        } else {

                            $lat = $watcher.Position.Location.Latitude
                            $lon = $watcher.Position.Location.Longitude

                            $culture =
                                [System.Globalization.CultureInfo]::InvariantCulture

                            Write-Output (
                                "PAW_LAT=" +
                                $lat.ToString($culture)
                            )

                            Write-Output (
                                "PAW_LON=" +
                                $lon.ToString($culture)
                            )
                        }

                        $watcher.Stop()

                        """;


                ProcessBuilder processBuilder =
                        new ProcessBuilder(
                                "powershell.exe",
                                "-NoProfile",
                                "-NonInteractive",
                                "-ExecutionPolicy",
                                "Bypass",
                                "-Command",
                                script
                        );


                processBuilder.redirectErrorStream(true);


                Process process =
                        processBuilder.start();


                String output =
                        new String(
                                process
                                        .getInputStream()
                                        .readAllBytes(),

                                StandardCharsets.UTF_8
                        );


                int exitCode =
                        process.waitFor();


                System.out.println(
                        "========== DEVICE LOCATION OUTPUT =========="
                );

                System.out.println(output);

                System.out.println(
                        "PowerShell exit code: "
                                + exitCode
                );

                System.out.println(
                        "============================================"
                );


                if (output.contains("PAW_NO_LOCATION")) {

                    callback.accept(
                            LocationResult.failure(
                                    "Windows could not determine your current location."
                            )
                    );

                    return;
                }


                Double latitude = null;
                Double longitude = null;


                /*
                 * Parse ONLY our own prefixed output lines.
                 *
                 * Ignore every other PowerShell line.
                 */
                for (String rawLine : output.split("\\R")) {

                    String line =
                            rawLine.trim();


                    if (line.startsWith("PAW_LAT=")) {

                        String value =
                                line.substring(
                                                "PAW_LAT=".length()
                                        )
                                        .trim();


                        try {

                            latitude =
                                    Double.parseDouble(value);

                        }

                        catch (NumberFormatException e) {

                            System.err.println(
                                    "Invalid latitude returned: "
                                            + value
                            );
                        }
                    }


                    else if (line.startsWith("PAW_LON=")) {

                        String value =
                                line.substring(
                                                "PAW_LON=".length()
                                        )
                                        .trim();


                        try {

                            longitude =
                                    Double.parseDouble(value);

                        }

                        catch (NumberFormatException e) {

                            System.err.println(
                                    "Invalid longitude returned: "
                                            + value
                            );
                        }
                    }
                }


                /*
                 * Both coordinates are required.
                 */
                if (latitude == null
                        || longitude == null) {

                    callback.accept(
                            LocationResult.failure(
                                    "Windows location service did not return usable coordinates."
                            )
                    );

                    return;
                }


                /*
                 * Basic coordinate validation.
                 */
                if (!Double.isFinite(latitude)
                        || !Double.isFinite(longitude)
                        || latitude < -90
                        || latitude > 90
                        || longitude < -180
                        || longitude > 180
                        || (latitude == 0
                        && longitude == 0)) {

                    callback.accept(
                            LocationResult.failure(
                                    "Windows returned invalid coordinates."
                            )
                    );

                    return;
                }


                callback.accept(
                        LocationResult.success(
                                latitude,
                                longitude
                        )
                );

            }

            catch (Exception e) {

                e.printStackTrace();


                callback.accept(
                        LocationResult.failure(
                                "Could not access Windows location service."
                        )
                );
            }

        });


        thread.setName(
                "pawconnect-device-location"
        );

        thread.setDaemon(true);

        thread.start();
    }


    public record LocationResult(
            boolean success,
            double latitude,
            double longitude,
            String message
    ) {


        public static LocationResult success(
                double latitude,
                double longitude
        ) {

            return new LocationResult(
                    true,
                    latitude,
                    longitude,
                    null
            );
        }


        public static LocationResult failure(
                String message
        ) {

            return new LocationResult(
                    false,
                    0,
                    0,
                    message
            );
        }
    }
}