pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }

  parameters {
    string(name: 'SDK_VERSION', defaultValue: '', description: 'Optional. If set, updates version.properties SDK_VERSION (X.Y.Z-SNAPSHOT publishes snapshots; X.Y.Z publishes to staging)')
    choice(name: 'BUILD_VARIANT', choices: ['release', 'staging', 'debug'], description: 'Build type')
    booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Run unit tests')
    booleanParam(name: 'RUN_LINT', defaultValue: false, description: 'Run Android lint')
    booleanParam(name: 'PUBLISH', defaultValue: false, description: 'Publish to Sonatype (snapshots if -SNAPSHOT, else staging)')
  }

  environment {
    GRADLE_OPTS = '-Dorg.gradle.jvmargs=-Xmx3g -Dfile.encoding=UTF-8'
    CI = 'true'
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Environment') {
      steps {
        sh '''
          set -euxo pipefail
          java -version
          ./gradlew --version
        '''
      }
    }

    stage('Set SDK version (optional)') {
      when { expression { return params.SDK_VERSION?.trim() } }
      steps {
        script {
          def v = params.SDK_VERSION.trim()
          sh """
            set -euxo pipefail
            file="version.properties"
            test -f "\$file"
            perl -i -pe 's/^SDK_VERSION=.*/SDK_VERSION=${v}/' "\$file"
            echo "SDK_VERSION now:"
            grep '^SDK_VERSION=' "\$file"
          """
        }
      }
    }

    stage('Build') {
      steps {
        script {
          def variantCap = params.BUILD_VARIANT.substring(0, 1).toUpperCase() + params.BUILD_VARIANT.substring(1)
          sh """
            set -euxo pipefail
            ./gradlew clean assemble${variantCap} --stacktrace
          """
        }
      }
    }

    stage('Unit tests') {
      when { expression { return params.RUN_TESTS } }
      steps {
        sh '''
          set -euxo pipefail
          ./gradlew test --stacktrace
        '''
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: '**/build/test-results/test*/TEST-*.xml'
        }
      }
    }

    stage('Lint') {
      when { expression { return params.RUN_LINT } }
      steps {
        sh '''
          set -euxo pipefail
          ./gradlew lint --stacktrace
        '''
      }
    }

    stage('Archive artifacts') {
      steps {
        archiveArtifacts artifacts: '**/build/outputs/aar/*.aar', fingerprint: true, allowEmptyArchive: true
        archiveArtifacts artifacts: '**/build/outputs/mapping/**', fingerprint: false, allowEmptyArchive: true
        archiveArtifacts artifacts: '**/build/reports/**', fingerprint: false, allowEmptyArchive: true
      }
    }

    stage('Publish (snapshots or staging)') {
      when { expression { return params.PUBLISH } }

      environment {
        OSSRH_TOKEN_USERNAME = credentials('OSSRH_TOKEN_USERNAME')
        OSSRH_TOKEN_SECRET   = credentials('OSSRH_TOKEN_SECRET')
        SIGNING_KEY_ID       = credentials('SIGNING_KEY_ID')
        SIGNING_PASSWORD     = credentials('SIGNING_PASSWORD')
        SIGNING_KEYRING_FILE = credentials('SIGNING_SECRET_KEY_RING_FILE') // secret-file credential path
      }

      steps {
        sh '''
          set -euxo pipefail

          if grep -q '^SDK_VERSION=.*-SNAPSHOT' version.properties; then
            echo "Publishing SNAPSHOT (Central Portal snapshots repo expected)"
          else
            echo "Publishing STABLE (OSSRH staging API expected)"
          fi

          ./gradlew publishReleasePublicationToSonatypeRepository \
            -PossrhTokenUsername="$OSSRH_TOKEN_USERNAME" \
            -PossrhTokenSecret="$OSSRH_TOKEN_SECRET" \
            -Psigning.keyId="$SIGNING_KEY_ID" \
            -Psigning.password="$SIGNING_PASSWORD" \
            -Psigning.secretKeyRingFile="$SIGNING_KEYRING_FILE" \
            --stacktrace
        '''
      }
    }
  }

  post {
    always {
      cleanWs(deleteDirs: true, notFailBuild: true)
    }
  }
}
