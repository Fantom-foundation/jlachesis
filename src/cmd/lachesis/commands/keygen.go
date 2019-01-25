package commands

import (
	"fmt"
	"io/ioutil"
	"os"
	"path"

	"github.com/Fantom-foundation/go-lachesis/src/crypto"
	"github.com/spf13/cobra"
)

var (
	privKeyFile           string
	pubKeyFile            string
	config                = NewDefaultCLIConfig()
	defaultPrivateKeyFile = fmt.Sprintf("%s/priv_key.pem", config.Lachesis.DataDir)
	defaultPublicKeyFile  = fmt.Sprintf("%s/key.pub", config.Lachesis.DataDir)
)

// NewKeygenCmd produces a KeygenCmd which creates a key pair
func NewKeygenCmd() *cobra.Command {
	cmd := &cobra.Command{
		Use:   "keygen",
		Short: "Create new key pair",
		RunE:  keygen,
	}
	AddKeygenFlags(cmd)
	return cmd
}

//AddKeygenFlags adds flags to the keygen command
func AddKeygenFlags(cmd *cobra.Command) {
	cmd.Flags().StringVar(&privKeyFile, "pem", defaultPrivateKeyFile, "File where the private key will be written")
	cmd.Flags().StringVar(&pubKeyFile, "pub", defaultPublicKeyFile, "File where the public key will be written")
}
func keygen(cmd *cobra.Command, args []string) error {
	pemDump, err := crypto.GeneratePemKey()
	if err != nil {
		return fmt.Errorf("error generating PemDump")
	}
	if err := os.MkdirAll(path.Dir(privKeyFile), 0700); err != nil {
		return fmt.Errorf("writing private key: %s", err)
	}

	_, err = os.Stat(privKeyFile)
	if err == nil {
		return fmt.Errorf("A key already lives under: %s", path.Dir(privKeyFile))
	}

	if err := ioutil.WriteFile(privKeyFile, []byte(pemDump.PrivateKey), 0666); err != nil {
		return fmt.Errorf("writing private key: %s", err)
	}
	fmt.Printf("Your private key has been saved to: %s\n", privKeyFile)
	if err := os.MkdirAll(path.Dir(pubKeyFile), 0700); err != nil {
		return fmt.Errorf("writing public key: %s", err)
	}
	if err := ioutil.WriteFile(pubKeyFile, []byte(pemDump.PublicKey), 0666); err != nil {
		return fmt.Errorf("writing public key: %s", err)
	}
	fmt.Printf("Your public key has been saved to: %s\n", pubKeyFile)
	return nil
}
