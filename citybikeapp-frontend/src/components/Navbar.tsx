import { CloseIcon, HamburgerIcon } from "@chakra-ui/icons";
import { Box, Button, Flex, HStack, IconButton, Link, useColorModeValue } from "@chakra-ui/react";
import { Link as ReactRouterLink } from "react-router-dom";
import React from "react";

const Navbar = ({ isMobileOpen, onToggle }: { isMobileOpen: boolean, onToggle: () => void }) => {
  return (
    <Box bg={useColorModeValue("gray.50", "gray.900")} p={4}>
      <Flex h={8} align={"center"} justify={"space-between"}>
        <IconButton
          size={"md"}
          icon={isMobileOpen ? <CloseIcon /> : <HamburgerIcon />}
          display={{ md: "none" }}
          aria-label={"Toggle sidebar"}
          onClick={onToggle}
        />
        <HStack spacing={8}>
          <HStack
            spacing={4}
            display={{ base: "none", md: "flex" }}
          >
            <Link as={ReactRouterLink} to="/stations"><Button>Stations</Button></Link>
            <Link as={ReactRouterLink} to="/journeys"><Button>Journeys</Button></Link>
          </HStack>
        </HStack>
      </Flex>
    </Box>
  );
};

export default Navbar;
