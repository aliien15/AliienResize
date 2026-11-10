#!/usr/bin/env python3
"""Check the final shaded JAR, independent of Maven's dependency classpath."""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parents[1]
NS = {'m': 'http://maven.apache.org/POM/4.0.0'}


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def verify(artifact, version, kotlin_version):
    with ZipFile(artifact) as jar:
        names = set(jar.namelist())
        for entry in (
            'com/aliiensmp/aliienResize/AliienResize.class',
            'com/aliiensmp/aliienresize/core/AliienCore.class',
            'com/aliiensmp/aliienresize/core/lib/hikari/HikariConfig.class',
            'com/aliiensmp/aliienresize/core/lib/boostedyaml/YamlDocument.class',
            'com/aliiensmp/aliienresize/lib/acf/PaperCommandManager.class',
            'com/aliiensmp/aliienresize/bstats/bukkit/Metrics.class',
        ):
            require(entry in names, f'Missing packaged class: {entry}')
        for prefix in (
            'com/aliiensmp/core/', 'com/zaxxer/hikari/', 'dev/dejvokep/boostedyaml/',
            'co/aikar/commands/', 'org/bstats/', 'org/bukkit/', 'io/papermc/',
            'me/clip/placeholderapi/', 'kotlin/', 'org/junit/',
            'org/sqlite/', 'org/h2/', 'com/mysql/', 'org/mariadb/jdbc/',
            'com/aliiensmp/aliienresize/core/lib/mysql/',
            'com/aliiensmp/aliienresize/core/lib/mariadb/',
        ):
            require(not any(name.startswith(prefix) for name in names),
                    f'Unexpected bundled namespace: {prefix}')
        require(not any('coinsengine' in name.lower() for name in names),
                'Removed CoinsEngine adapter is still packaged')
        descriptor = jar.read('plugin.yml').decode('utf-8')
        require('${' not in descriptor, 'Unresolved Maven property in plugin.yml')
        require(re.search(r"(?m)^version: ['\"]?" + re.escape(version) + r"['\"]?$", descriptor),
                'Plugin version does not match pom.xml')
        require('main: com.aliiensmp.aliienResize.AliienResize' in descriptor,
                'Unexpected plugin entrypoint')
        require('folia-supported: true' in descriptor, 'Folia support flag missing')
        require('CoinsEngine' not in descriptor, 'CoinsEngine soft dependency remains')
        libraries_section = descriptor.split('\nlibraries:\n', 1)
        require(len(libraries_section) == 2, 'Missing runtime libraries')
        libraries = re.findall(r'^  - (\S+)\s*$',
                               libraries_section[1].split('\n\n', 1)[0], re.MULTILINE)
        expected = {
            f'org.jetbrains.kotlin:kotlin-stdlib:{kotlin_version}',
            'com.mysql:mysql-connector-j:26.10.0',
            'org.mariadb.jdbc:mariadb-java-client:4.0.0',
            'org.xerial:sqlite-jdbc:3.54.1.0',
            'com.h2database:h2:2.2.224',
        }
        require(len(libraries) == len(expected) and set(libraries) == expected,
                f'Unexpected runtime libraries: {libraries}')
        for resource in ('settings.yml', 'messages.yml', 'sizes.yml',
                         'main-menu.yml', 'confirmation-menu.yml'):
            require(resource in names, f'Missing default config: {resource}')
        # Stale providers here would fail ServiceLoader before runtime libraries can help.
        service = 'META-INF/services/java.sql.Driver'
        if service in names:
            for line in jar.read(service).decode('utf-8').splitlines():
                provider = line.split('#', 1)[0].strip()
                require(not provider or provider.replace('.', '/') + '.class' in names,
                        f'Dangling JDBC service provider: {provider}')
    print(f'Packaging checks passed: {artifact.name}')


if __name__ == '__main__':
    pom = ET.parse(ROOT / 'pom.xml').getroot()
    version = pom.findtext('m:version', namespaces=NS)
    kotlin_version = pom.findtext('m:properties/m:kotlin.version', namespaces=NS)
    artifact = Path(sys.argv[1]) if len(sys.argv) > 1 else ROOT / 'target' / f'AliienResize-{version}.jar'
    verify(artifact, version, kotlin_version)
